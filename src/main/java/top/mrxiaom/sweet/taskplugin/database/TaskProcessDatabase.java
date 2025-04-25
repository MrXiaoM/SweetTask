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
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static top.mrxiaom.sweet.taskplugin.SweetTask.DEBUG;

public class TaskProcessDatabase extends AbstractPluginHolder implements IDatabase, Listener {
    private String TABLE_NAME, REFRESH_TABLE_NAME;
    private final Map<String, PlayerCache> caches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean onlineMode, disabling;
    private long nextRefresh = 0;
    private boolean loadFlag;
    public TaskProcessDatabase(SweetTask plugin) {
        super(plugin, true);
        registerEvents();
        registerBungee();
        plugin.getScheduler().runTaskTimerAsync(() -> {
            for (PlayerCache cache : caches.values()) {
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
        REFRESH_TABLE_NAME = (s + "task_refresh").toUpperCase();
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
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + REFRESH_TABLE_NAME + "`(" +
                        "`player` varchar(48) PRIMARY KEY," + // 玩家名
                        "`count_daily` int," + // 每日任务 已刷新次数
                        "`expire_time_daily` timestamp," + // 每日任务 下一次可刷新时间
                        "`count_weekly` int," + // 每周任务 已刷新次数
                        "`expire_time_weekly` timestamp," + // 每周任务 下一次可刷新时间
                        "`count_monthly` int," + // 每月任务 已刷新次数
                        "`expire_time_monthly` timestamp" + // 每月任务 下一次可刷新时间
                ");"
        )) {
            ps.execute();
        }
        if (loadFlag) {
            loadFlag = false;
            plugin.getScheduler().runTaskAsync(() -> {
                try (Connection connection = plugin.getConnection()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String id = id(player);
                        refreshCache(connection, id, player);
                    }
                } catch (SQLException e) {
                    warn(e);
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
                PlayerCache cache = caches.remove(id);
                if (cache != null && cache.player.isOnline()) {
                    plugin.getScheduler().runTaskAsync(() -> getTasks(cache.player));
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
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        cleanExpiredTasks(player);
        PlayerCache cache = getTasks(player);
        TaskManager.inst().checkTasksAsync(cache);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        submitCache(player);
        caches.remove(id(player));
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        submitCache(player);
        caches.remove(id(player));
    }

    @Override
    public void onDisable() {
        disabling = true;
        for (PlayerCache cache : caches.values()) {
            submitCache(cache, false);
        }
        caches.clear();
    }

    public void submitRefreshCount(Player player, int refreshCountDaily, int refreshCountWeekly, int refreshCountMonthly) {
        String id = id(player);
        String sentence;
        boolean mysql = plugin.options.database().isMySQL();
        if (mysql) {
            sentence = "INSERT INTO `" + TABLE_NAME + "`" +
                    "(`player`, `count_daily`, `expire_time_daily`, `count_weekly`, `expire_time_weekly`, `count_monthly`, `expire_time_monthly`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?) " +
                    "on duplicate key update `count_daily`=?, `expire_time_daily`=?, " +
                    "`count_weekly`=?, `expire_time_weekly`=? " +
                    "`count_monthly`=?, `expire_time_monthly`=?;";
        } else if (plugin.options.database().isSQLite()) {
            sentence = "INSERT OR REPLACE INTO `" + TABLE_NAME + "`" +
                    "(`player`, `count_daily`, `expire_time_daily`, `count_weekly`, `expire_time_weekly`, `count_monthly`, `expire_time_monthly`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?);";
        } else return;
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(sentence)) {
            TaskManager manager = TaskManager.inst();
            Timestamp timestampDaily = Timestamp.valueOf(manager.nextOutdate(EnumTaskType.DAILY));
            Timestamp timestampWeekly = Timestamp.valueOf(manager.nextOutdate(EnumTaskType.WEEKLY));
            Timestamp timestampMonthly = Timestamp.valueOf(manager.nextOutdate(EnumTaskType.MONTHLY));
            ps.setString(1, id);
            ps.setInt(2, refreshCountDaily);
            ps.setTimestamp(3, timestampDaily);
            ps.setInt(4, refreshCountWeekly);
            ps.setTimestamp(5, timestampWeekly);
            ps.setInt(6, refreshCountMonthly);
            ps.setTimestamp(7, timestampMonthly);
            if (mysql) {
                ps.setInt(8, refreshCountDaily);
                ps.setTimestamp(9, timestampDaily);
                ps.setInt(10, refreshCountWeekly);
                ps.setTimestamp(11, timestampWeekly);
                ps.setInt(12, refreshCountMonthly);
                ps.setTimestamp(13, timestampMonthly);
            }
            ps.execute();
        } catch (SQLException e) {
            warn(e);
        }
    }

    /**
     * 从数据库拉取玩家当前任务列表及其子任务数据。<br>
     * 带有缓存，缓存将在玩家进入或离开服务器时到期。
     */
    public PlayerCache getTasks(Player player) {
        String id = id(player);
        PlayerCache cache = caches.get(id);
        if (cache != null) return cache;
        try (Connection conn = plugin.getConnection()) {
            cache = refreshCache(conn, id, player);
        } catch (SQLException e) {
            warn(e);
        }
        if (cache != null) {
            return cache;
        } else {
            return new PlayerCache(player, new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        }
    }

    private PlayerCache refreshCache(Connection conn, String id, Player player) throws SQLException {
        Map<String, TaskCache> tasksMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // 获取玩家的任务数据
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
                    if (DEBUG) plugin.warn("  任务" + taskId + ": 子任务 " + subTaskId + " 已到期 (" + expireTime.getTime() + ")");
                    // 已过期任务不加入结果中
                    continue;
                }
                TaskCache task = tasksMap.computeIfAbsent(taskId, it -> new TaskCache(it, expireTime.toLocalDateTime()));
                task.put(subTaskId, data);
            }
        }
        PlayerCache cache = new PlayerCache(player, tasksMap);
        if (DEBUG) {
            plugin.info("玩家 " + player.getName() + " 的数据已拉取");
            for (TaskCache task : tasksMap.values()) {
                plugin.info("  任务 " + task.taskId + " 的数据如下:");
                for (Map.Entry<String, Integer> entry : task.subTaskData.entrySet()) {
                    plugin.info("    " + entry.getKey() + " = " + entry.getValue());
                }
            }
        }
        // 获取玩家的刷新次数以及到期时间数据
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + REFRESH_TABLE_NAME + "` " +
                "WHERE `player`=?")) {
            ps.setString(1, id);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    int refreshCountDaily = result.getInt("count_daily");
                    int refreshCountWeekly = result.getInt("count_weekly");
                    int refreshCountMonthly = result.getInt("count_monthly");
                    LocalDateTime refreshCountExpireDaily = result.getTimestamp("expire_time_daily").toLocalDateTime();
                    LocalDateTime refreshCountExpireWeekly = result.getTimestamp("expire_time_weekly").toLocalDateTime();
                    LocalDateTime refreshCountExpireMonthly = result.getTimestamp("expire_time_monthly").toLocalDateTime();
                    cache.setRefreshCount(
                            refreshCountDaily, refreshCountExpireDaily,
                            refreshCountWeekly, refreshCountExpireWeekly,
                            refreshCountMonthly, refreshCountExpireMonthly
                    );
                }
            }
        }
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
        PlayerCache cache = caches.get(id);
        if (cache != null) {
            submitCache(cache, false);
        }
    }

    public void submitCache(PlayerCache cache, boolean clear) {
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
        if (DEBUG) plugin.info("正在提交玩家 " + cache.player.getName() + " 的任务缓存");
        try (Connection conn = plugin.getConnection()) {
            if (clear) clearTasks(conn, id); // 删除所有任务数据
            try (PreparedStatement ps = conn.prepareStatement(sentence)) {
                // 添加本地的任务数据
                for (TaskCache taskCache : cache.tasks.values()) {
                    for (Map.Entry<String, Integer> entry : taskCache.data()) {
                        addBatchCache(ps, mysql, id, taskCache.taskId,
                                entry.getKey(), entry.getValue(), taskCache.expireTime);
                    }
                }
                ps.executeBatch();
            }
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
                PlayerCache removed = caches.remove(id);
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

    private void clearTasks(Connection conn, String player) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM TABLE `" + TABLE_NAME +"` WHERE `player`=?;"
        )) {
            ps.setString(1, player);
            ps.execute();
        }
    }
}
