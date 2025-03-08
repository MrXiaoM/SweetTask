package top.mrxiaom.sweet.taskplugin.database.entry;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PlayerCache {
    public final Player player;
    public final Map<String, TaskCache> tasks;
    private Long nextSubmit = null;
    private int refreshCount = 0;
    private LocalDateTime refreshCountExpireTime;

    public PlayerCache(Player player, Map<String, TaskCache> tasks) {
        this.player = player;
        this.tasks = tasks;
        removeOutdatedTasks();
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
    }

    public void setRefreshCount(int refreshCount, LocalDateTime refreshCountExpireTime) {
        this.refreshCount = refreshCount;
        this.refreshCountExpireTime = refreshCountExpireTime;
    }

    public void submitRefresh() {
        TaskManager manager = TaskManager.inst();
        if (LocalDateTime.now().isAfter(refreshCountExpireTime)) {
            refreshCountExpireTime = manager.nextOutdate(EnumTaskType.DAILY);
            refreshCount = 0;
        }
        refreshCount++;
        manager.plugin.getDatabase().submitRefreshCount(player, refreshCount, refreshCountExpireTime);
    }

    public boolean canRefresh(EnumTaskType type, int limit) {
        TaskManager manager = TaskManager.inst();
        for (TaskCache taskCache : tasks.values()) {
            LoadedTask task = manager.getTask(taskCache.taskId);
            if (task != null) {
                if (task.type.equals(type) && taskCache.hasDone()) {
                    return false;
                }
            }
        }
        if (LocalDateTime.now().isAfter(refreshCountExpireTime)) {
            refreshCountExpireTime = manager.nextOutdate(EnumTaskType.DAILY);
            refreshCount = 0;
        }
        return refreshCount < limit;
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
}
