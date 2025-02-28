package top.mrxiaom.sweet.taskplugin.database;

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
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.SubTaskCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TaskProcessDatabase extends AbstractPluginHolder implements IDatabase, Listener {
    private String TABLE_NAME;
    private final Map<String, TaskCache> caches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean onlineMode, disabling;
    public TaskProcessDatabase(SweetTask plugin) {
        super(plugin);
        registerEvents();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (TaskCache cache : caches.values()) {
                if (cache.needSubmit()) {
                    submitCache(cache.player);
                }
            }
        }, 30 * 20L, 30 * 20L);
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
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        if (disabling) return;
        Player player = e.getPlayer();
        caches.remove(id(player));
        cleanExpiredTasks(player);
        getTasks(player);
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
        Map<String, SubTaskCache> tasks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` " +
                    "WHERE `player`=?")) {
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
                    SubTaskCache task = tasks.get(taskId);
                    if (task == null) {
                        task = new SubTaskCache(taskId, expireTime.toLocalDateTime());
                    }
                    task.put(subTaskId, data);
                    tasks.put(taskId, task);
                }
            }
            cache = new TaskCache(player, tasks);
            caches.put(id, cache);
        } catch (SQLException e) {
            warn(e);
        }
        if (cache != null) {
            return cache;
        } else {
            return new TaskCache(player, tasks);
        }
    }

    public void cleanExpiredTasks(Player player) {
        String id = id(player);
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
        } catch (SQLException e) {
            warn(e);
        }
    }

    public void addTask(Player player, LoadedTask task, LocalDateTime expireTime) {
        String id = id(player);
        try (Connection conn = plugin.getConnection()) {
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
        } catch (SQLException e) {
            warn(e);
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
}
