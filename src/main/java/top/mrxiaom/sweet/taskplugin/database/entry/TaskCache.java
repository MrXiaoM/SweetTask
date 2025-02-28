package top.mrxiaom.sweet.taskplugin.database.entry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class TaskCache {
    public final Player player;
    public final Map<String, SubTaskCache> tasks;
    private Long nextSubmit = null;

    public TaskCache(Player player, Map<String, SubTaskCache> tasks) {
        this.player = player;

        this.tasks = tasks;
    }

    public void scheduleSubmit(int seconds) {
        if (nextSubmit == null) {
            nextSubmit = System.currentTimeMillis() + seconds * 1000L;
        }
    }

    @Nullable
    public LocalDateTime nextSubmitTime() {
        if (nextSubmit == null) return null;
        return new Timestamp(nextSubmit).toLocalDateTime();
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
