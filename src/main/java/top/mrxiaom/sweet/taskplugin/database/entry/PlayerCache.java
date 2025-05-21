package top.mrxiaom.sweet.taskplugin.database.entry;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerCache {
    public final Player player;
    public final Map<String, TaskCache> tasks;
    private Long nextSubmit = null;
    private int refreshCountDaily;
    private int refreshCountWeekly;
    private int refreshCountMonthly;
    private LocalDateTime refreshCountExpireDaily;
    private LocalDateTime refreshCountExpireWeekly;
    private LocalDateTime refreshCountExpireMonthly;

    public PlayerCache(Player player, Map<String, TaskCache> tasks) {
        this.player = player;
        this.tasks = tasks;
    }

    public void scheduleSubmit(int seconds) {
        if (nextSubmit == null) {
            nextSubmit = System.currentTimeMillis() + seconds * 1000L;
        }
    }

    public void addTask(LoadedTask task, LocalDateTime expireTime) {
        TaskCache cache = new TaskCache(task.id, expireTime);
        cache.put(task.id, 0);
        for (int i = 0; i < task.subTasks.size(); i++) {
            ITask subTask = task.subTasks.get(i);
            cache.put(i, subTask.type(), 0);
        }
        tasks.put(task.id, cache);
    }

    public void setRefreshCount(
            int refreshCountDaily, LocalDateTime refreshCountExpireDaily,
            int refreshCountWeekly, LocalDateTime refreshCountExpireWeekly,
            int refreshCountMonthly, LocalDateTime refreshCountExpireMonthly
    ) {
        this.refreshCountDaily = refreshCountDaily;
        this.refreshCountExpireDaily = refreshCountExpireDaily;
        this.refreshCountWeekly = refreshCountWeekly;
        this.refreshCountExpireWeekly = refreshCountExpireWeekly;
        this.refreshCountMonthly = refreshCountMonthly;
        this.refreshCountExpireMonthly = refreshCountExpireMonthly;
    }

    /**
     * 提交刷新，检查过期时间的同时，使刷新次数+1
     */
    public void submitRefresh(EnumTaskType type, Runnable done) {
        TaskManager manager = TaskManager.inst();
        LocalDateTime now = LocalDateTime.now();
        if (refreshCountExpireDaily == null || now.isAfter(refreshCountExpireDaily)) {
            refreshCountExpireDaily = manager.nextOutdate(EnumTaskType.DAILY);
            refreshCountDaily = 0;
        }
        if (refreshCountExpireWeekly == null || now.isAfter(refreshCountExpireWeekly)) {
            refreshCountExpireWeekly = manager.nextOutdate(EnumTaskType.WEEKLY);
            refreshCountWeekly = 0;
        }
        if (refreshCountExpireMonthly == null || now.isAfter(refreshCountExpireMonthly)) {
            refreshCountExpireMonthly = manager.nextOutdate(EnumTaskType.MONTHLY);
            refreshCountMonthly = 0;
        }
        switch (type) {
            case DAILY:
                refreshCountDaily++;
                break;
            case WEEKLY:
                refreshCountWeekly++;
                break;
            case MONTHLY:
                refreshCountMonthly++;
                break;
            default:
                return;
        }
        manager.plugin.getDatabase().submitRefreshCount(player, refreshCountDaily, refreshCountWeekly, refreshCountMonthly);
        tasks.clear();
        manager.checkTasksAsync(this, done);
    }

    public int getRefreshCountDaily() {
        if (refreshCountExpireDaily == null || LocalDateTime.now().isAfter(refreshCountExpireDaily)) {
            refreshCountExpireDaily = TaskManager.inst().nextOutdate(EnumTaskType.DAILY);
            refreshCountDaily = 0;
        }
        return refreshCountDaily;
    }

    public int getRefreshCountWeekly() {
        if (refreshCountExpireWeekly == null || LocalDateTime.now().isAfter(refreshCountExpireWeekly)) {
            refreshCountExpireWeekly = TaskManager.inst().nextOutdate(EnumTaskType.WEEKLY);
            refreshCountWeekly = 0;
        }
        return refreshCountWeekly;
    }

    public int getRefreshCountMonthly() {
        if (refreshCountExpireMonthly == null || LocalDateTime.now().isAfter(refreshCountExpireMonthly)) {
            refreshCountExpireMonthly = TaskManager.inst().nextOutdate(EnumTaskType.MONTHLY);
            refreshCountMonthly = 0;
        }
        return refreshCountMonthly;
    }

    public Boolean canRefresh(EnumTaskType type) {
        switch (type) {
            case DAILY:
                return canRefresh(type, TaskManager.inst().getDailyMaxRefreshCount(player));
            case WEEKLY:
                return canRefresh(type, TaskManager.inst().getWeeklyMaxRefreshCount(player));
            case MONTHLY:
                return canRefresh(type, TaskManager.inst().getMonthlyMaxRefreshCount(player));
        }
        return false;
    }

    /**
     * 是否可以刷新任务列表
     * @param type 任务类型
     * @param limit 限制数量
     * @return <ul>
     *     <li><code>null</code> 代表已经完成过任务了</li>
     *     <li><code>false</code> 代表刷新次数已用尽</li>
     *     <li><code>true</code> 代表可以刷新</li>
     * </ul>
     */
    public Boolean canRefresh(EnumTaskType type, int limit) {
        TaskManager manager = TaskManager.inst();
        for (TaskCache taskCache : tasks.values()) {
            LoadedTask task = manager.getTask(taskCache.taskId);
            if (task != null) {
                if (task.type.equals(type) && taskCache.hasDone()) {
                    return null;
                }
            }
        }
        if (type.equals(EnumTaskType.DAILY)) {
            return getRefreshCountDaily() < limit;
        }
        if (type.equals(EnumTaskType.WEEKLY)) {
            return getRefreshCountWeekly() < limit;
        }
        if (type.equals(EnumTaskType.MONTHLY)) {
            return getRefreshCountMonthly() < limit;
        }
        return false;
    }

    public int getRefreshCount(EnumTaskType type) {
        switch (type) {
            case DAILY:
                return getRefreshCountDaily();
            case WEEKLY:
                return getRefreshCountWeekly();
            case MONTHLY:
                return getRefreshCountMonthly();
        }
        return 0;
    }

    public int getRefreshCountRemain(EnumTaskType type) {
        switch (type) {
            case DAILY: {
                int max = TaskManager.inst().getDailyMaxRefreshCount(player);
                return Math.max(0, max - getRefreshCountDaily());
            }
            case WEEKLY: {
                int max = TaskManager.inst().getWeeklyMaxRefreshCount(player);
                return Math.max(0, max - getRefreshCountWeekly());
            }
            case MONTHLY: {
                int max = TaskManager.inst().getMonthlyMaxRefreshCount(player);
                return Math.max(0, max - getRefreshCountMonthly());
            }
        }
        return 0;
    }

    @Nullable
    public LocalDateTime nextSubmitTime() {
        if (nextSubmit == null) return null;
        return new Timestamp(nextSubmit).toLocalDateTime();
    }

    public boolean removeOutdatedTasks() {
        boolean modified = false;
        LocalDateTime now = LocalDateTime.now();
        List<String> keys = Lists.newArrayList(tasks.keySet());
        for (String key : keys) {
            TaskCache sub = tasks.get(key);
            if (now.isAfter(sub.expireTime)) {
                if (SweetTask.DEBUG) {
                    SweetTask.getInstance().info("[" + player.getName() + "] 已移除过期任务 " + key + " (到期时间: " + sub.expireTime + ")");
                }
                tasks.remove(key);
                modified = true;
            }
        }
        return modified;
    }

    public boolean needSubmit() {
        if (nextSubmit == null) return false;
        if (System.currentTimeMillis() > nextSubmit) {
            nextSubmit = null;
            return true;
        }
        return false;
    }

    public List<Pair<LoadedTask, TaskCache>> getTasksByType(EnumTaskType type) {
        TaskManager manager = TaskManager.inst();
        List<Pair<LoadedTask, TaskCache>> tasks = new ArrayList<>();
        for (TaskCache cache : this.tasks.values()) {
            LoadedTask task = manager.getTask(cache.taskId);
            if (task != null && task.type.equals(type)) {
                tasks.add(Pair.of(task, cache));
            }
        }
        return tasks;
    }
}
