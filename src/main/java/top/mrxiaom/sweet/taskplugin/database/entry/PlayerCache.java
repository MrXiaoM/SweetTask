package top.mrxiaom.sweet.taskplugin.database.entry;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PlayerCache {
    public final Player player;
    public final Map<String, TaskCache> tasks;
    private Long nextSubmit = null;

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
