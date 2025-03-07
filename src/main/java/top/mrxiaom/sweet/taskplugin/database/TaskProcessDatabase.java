package top.mrxiaom.sweet.taskplugin.database;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.utils.Bytes;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.SubTaskCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TaskProcessDatabase extends AbstractPluginHolder implements IDatabase, Listener {
    private String TABLE_NAME;
    private final Map<String, TaskCache> caches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean onlineMode, disabling;
    private long nextRefresh = 0;
    private boolean loadFlag = false;
    public TaskProcessDatabase(SweetTask plugin) {
        super(plugin);
        registerEvents();
        registerBungee();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (TaskCache cache : caches.values()) {
                if (cache.needSubmit()) {
                    submitCache(cache.player);
                }
            }
        }, 30 * 20L, 30 * 20L);
        loadFlag = !Bukkit.getOnlinePlayers().isEmpty();
    }

    @Override
    public int priority() {
        return 999;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    private String id(OfflinePlayer player) {
        if (isOnlineMode()) {
            return player.getUniqueId().toString();
        }
        return player.getName();
    }

    private OfflinePlayer fromId(String id) {
        if (isOnlineMode()) {
            UUID uuid = UUID.fromString(id);
            return Util.getOfflinePlayer(uuid).orElse(null);
        }
        return Util.getOfflinePlayer(id).orElse(null);
    }

    @Override
    public void reload(Connection conn, String s) throws SQLException {
        FileConfiguration config = plugin.getConfig();
        String onlineMode = config.getString("online-mode", "auto");
        if (onlineMode.equals("auto")) {
            this.onlineMode = Bukkit.getOnlineMode();
        } else {
            this.onlineMode = onlineMode.equals("true");
        }
        TABLE_NAME = (s + "task_process").toUpperCase();
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + TABLE_NAME + "`(" +
                        "`player` varchar(48)," + // 玩家名
                        "`task_id` varchar(48)," + // 主任务ID
                        "`sub_task_id` varchar(48)," + // 子任务ID，与主任务ID相同代表占位数据
                        "`data` int," + // 子任务数据值，占位数据为0
                        "`expire_time` timestamp," + // 过期时间
                        "PRIMARY KEY(`player`,`task_id`,`sub_task_id`)" +
                ");")) {
            ps.execute();
        }
        if (loadFlag) {
            loadFlag = false;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getTasks(player);
                }
            });
        }
    }

    @Override
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {
        if (subChannel.equals("SweetTaskRefreshCache")) {
            long now = System.currentTimeMillis();
            if (now < nextRefresh) return;
            nextRefresh = now + 3000L;
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                String id = in.readUTF();
                TaskCache cache = caches.remove(id);
                if (cache != null && cache.player.isOnline()) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin,
                            () -> getTasks(cache.player));
                }
            }
        }
    }

    public void noticeRefreshCache(List<String> ids) {
        if (ids.isEmpty()) return;
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) return;
        Bytes.sendByWhoeverOrNot("BungeeCord", Bytes.build(out -> {
            out.writeInt(ids.size());
            for (String id : ids) {
                out.writeUTF(id);
            }
        }, "Forward", "ALL", "SweetTaskRefreshCache"));
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        cleanExpiredTasks(player);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        submitCache(player);
        caches.remove(id(player));
    }

    @EventHandler
    public void on(PlayerKickEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        submitCache(player);
        caches.remove(id(player));
    }

    @Override
    public void onDisable() {
        disabling = true;
        for (TaskCache cache : caches.values()) {
            submitCache(cache);
        }
        caches.clear();
    }

    /**
     * 从数据库拉取玩家当前任务列表及其子任务数据。<br>
     * 带有缓存，缓存将在玩家进入或离开服务器时到期。
     */
    public TaskCache getTasks(Player player) {
        String id = id(player);
        TaskCache cache = caches.get(id);
        if (cache != null) return cache;
        try (Connection conn = plugin.getConnection()) {
            cache = refreshCache(conn, id, player);
        } catch (SQLException e) {
            warn(e);
        }
        if (cache != null) {
            return cache;
        } else {
            return new TaskCache(player, new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        }
    }

    private TaskCache refreshCache(Connection conn, String id, Player player) throws SQLException {
        Map<String, SubTaskCache> subTasksMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` " +
                "WHERE `player`=?")) {
            ps.setString(1, id);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                String taskId = result.getString("task_id");
                String subTaskId = result.getString("sub_task_id");
                int data = result.getInt("data");
                Timestamp expireTime = result.getTimestamp("expire_time");
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                if (now.after(expireTime)) {
                    // 已过期任务不加入结果中
                    continue;
                }
                SubTaskCache task = subTasksMap.get(taskId);
                if (task == null) {
                    task = new SubTaskCache(taskId, expireTime.toLocalDateTime());
                }
                task.put(subTaskId, data);
                subTasksMap.put(taskId, task);
            }
        }
        TaskCache cache = new TaskCache(player, subTasksMap);
        caches.put(id, cache);
        return cache;
    }

    public void cleanExpiredTasks(Player player) {
        String id = id(player);
        caches.remove(id);
        String sentence;
        if (plugin.options.database().isMySQL()) {
            sentence = "DELETE FROM `" + TABLE_NAME + "` WHERE `player`=? AND NOW() >= `expire_time`;";
        } else if (plugin.options.database().isSQLite()) {
            sentence = "DELETE FROM `" + TABLE_NAME + "` WHERE `player`=? AND datetime('now') >= `expire_time`;";
        } else return;
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(sentence)) {
            ps.setString(1, id);
            ps.execute();
            refreshCache(conn, id, player);
        } catch (SQLException e) {
            warn(e);
        }
    }

    public void addTask(Player player, LoadedTask task, LocalDateTime expireTime) {
        String id = id(player);
        try (Connection conn = plugin.getConnection()) {
            addTask(conn, id, task, expireTime);
            refreshCache(conn, id, player);
        } catch (SQLException e) {
            warn(e);
        }
    }

    private void addTask(Connection conn, String id, LoadedTask task, LocalDateTime expireTime) throws SQLException {
        List<String> list = new ArrayList<>();
        list.add(task.id);
        for (int i = 0; i < task.subTasks.size(); i++) {
            list.add(i + "-" + task.subTasks.get(i).type());
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO `" + TABLE_NAME + "`" +
                        "(`player`, `task_id`, `sub_task_id`, `data`, `expire_time`) " +
                        "VALUES(?, ?, ?, 0, ?);")) {
            for (String s : list) {
                // player
                ps.setString(1, id);
                // task_id
                ps.setString(2, task.id);
                // sub_task_id
                ps.setString(3, s);
                // expire_time
                ps.setTimestamp(4, Timestamp.valueOf(expireTime));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void submitCache(Player player) {
        String id = id(player);
        TaskCache cache = caches.get(id);
        if (cache != null) {
            submitCache(cache);
        }
    }

    public void submitCache(TaskCache cache) {
        String id = id(cache.player);
        String sentence;
        boolean mysql = plugin.options.database().isMySQL();
        if (mysql) {
            sentence = "INSERT INTO `" + TABLE_NAME + "`" +
                    "(`player`, `task_id`, `sub_task_id`, `data`, `expire_time`) " +
                    "VALUES(?, ?, ?, ?, ?) " +
                    "on duplicate key update `data`=?;";
        } else if (plugin.options.database().isSQLite()) {
            sentence = "INSERT OR REPLACE INTO `" + TABLE_NAME + "`" +
                    "(`player`, `task_id`, `sub_task_id`, `data`, `expire_time`) " +
                    "VALUES(?, ?, ?, ?, ?);";
        } else return;
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(sentence)) {
            for (SubTaskCache subTaskCache : cache.tasks.values()) {
                addBatchCache(ps, mysql, id, subTaskCache.taskId,
                        subTaskCache.taskId, 0, subTaskCache.expireTime);
                for (Map.Entry<String, Integer> entry : subTaskCache.subTaskData.entrySet()) {
                    addBatchCache(ps, mysql, id, subTaskCache.taskId,
                            entry.getKey(), entry.getValue(), subTaskCache.expireTime);
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            warn(e);
        }
    }
    private static void addBatchCache(PreparedStatement ps, boolean mysql, String player, String task, String subTask, int data, LocalDateTime expire) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expire)) return;
        ps.setString(1, player);
        ps.setString(2, task);
        ps.setString(3, subTask);
        ps.setInt(4, data);
        ps.setTimestamp(5, Timestamp.valueOf(expire));
        if (mysql) ps.setInt(6, data);
        ps.addBatch();
    }
    public void resetTask(LoadedTask task) {
        try (Connection conn = plugin.getConnection()) {
            plugin.info("[reset/" + task.id + "] 正在重置任务数据");
            Map<String, LocalDateTime> players = new HashMap<>();
            // 获取接了这些任务的玩家列表，及其到期时间
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT `player`, `expire_time` FROM `" + TABLE_NAME + "` " +
                    "WHERE `task_id`=? AND `sub_task_id`=?"
            )) {
                ps.setString(1, task.id);
                ps.setString(2, task.id);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    String player = resultSet.getString("player");
                    Timestamp expireTime = resultSet.getTimestamp("expire_time");
                    players.put(player, expireTime.toLocalDateTime());
                }
            }
            if (players.isEmpty()) {
                plugin.info("[reset/" + task.id + "] 没有人正在进行这个任务，无需重置");
                return;
            }
            // 从数据库中删除
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM `" + TABLE_NAME + "` " +
                    "WHERE `player`=? AND `task_id`=?"
            )) {
                for (String id : players.keySet()) {
                    ps.setString(1, id);
                    ps.setString(2, task.id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            plugin.info("[reset/" + task.id + "] 已从数据库中移除 " + players.size() + " 个玩家的任务数据");
            List<Pair<Player, String>> refreshList = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            // 重新添加任务到玩家
            for (Map.Entry<String, LocalDateTime> entry : players.entrySet()) {
                String id = entry.getKey();
                LocalDateTime expireTime = entry.getValue();
                addTask(conn, id, task, expireTime);
                TaskCache removed = caches.remove(id);
                if (removed != null && removed.player.isOnline()) {
                    refreshList.add(Pair.of(removed.player, id));
                } else {
                    ids.add(id);
                }
            }
            plugin.info("[reset/" + task.id + "] 重置完成，" + refreshList.size() + " 个在线玩家的数据缓存已计划刷新");
            // 如果需要的话，刷新缓存
            for (Pair<Player, String> pair : refreshList) {
                refreshCache(conn, pair.getValue(), pair.getKey());
            }
            noticeRefreshCache(ids);
        } catch (SQLException e) {
            warn(e);
        }
    }
}
