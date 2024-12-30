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
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class TaskProcessDatabase extends AbstractPluginHolder implements IDatabase, Listener {
    public static class TaskCache {
        public final String taskId;
        public final Map<String, Integer> subTaskData;
        public final LocalDateTime expireTime;

        public TaskCache(String taskId, LocalDateTime expireTime) {
            this.taskId = taskId;
            this.expireTime = expireTime;
            this.subTaskData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        public void put(String subTask, int data) {
            subTaskData.put(subTask, data);
        }

        @Nullable
        public Integer get(String subTask) {
            return subTaskData.get(subTask);
        }

        public int get(String subTask, int def) {
            return subTaskData.getOrDefault(subTask, def);
        }
    }
    private String TABLE_NAME;
    private final Map<String, Map<String, TaskCache>> caches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean onlineMode;
    public TaskProcessDatabase(SweetTask plugin) {
        super(plugin);
        registerEvents();
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
        Player player = e.getPlayer();
        caches.remove(id(player));
        cleanExpiredTasks(player);
        getTasks(player);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        caches.remove(id(e.getPlayer()));
    }

    @EventHandler
    public void on(PlayerKickEvent e) {
        caches.remove(id(e.getPlayer()));
    }

    /**
     * 从数据库拉取玩家当前任务列表及其子任务数据。<br>
     * 带有缓存，缓存将在玩家进入或离开服务器时到期。
     */
    public Map<String, TaskCache> getTasks(Player player) {
        String id = id(player);
        Map<String, TaskCache> cache = caches.get(id);
        if (cache != null) return cache;
        Map<String, TaskCache> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
                    TaskCache task = map.get(taskId);
                    if (task == null) {
                        task = new TaskCache(taskId, expireTime.toLocalDateTime());
                    }
                    task.put(subTaskId, data);
                    map.put(taskId, task);
                }
            }
            caches.put(id, map);
        } catch (SQLException e) {
            warn(e);
        }
        return map;
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
}
