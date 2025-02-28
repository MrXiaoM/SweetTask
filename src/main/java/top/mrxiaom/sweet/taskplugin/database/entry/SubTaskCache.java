package top.mrxiaom.sweet.taskplugin.database.entry;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.listeners.TaskWrapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class SubTaskCache {
    public final String taskId;
    public final Map<String, Integer> subTaskData;
    public final LocalDateTime expireTime;

    public SubTaskCache(String taskId, LocalDateTime expireTime) {
        this.taskId = taskId;
        this.expireTime = expireTime;
        this.subTaskData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public void put(String subTask, int data) {
        subTaskData.put(subTask, data);
    }

    public void put(int index, String subTask, int data) {
        put(index + "-" + subTask, data);
    }

    @Nullable
    public Integer get(int index, String subTask) {
        return subTaskData.get(index + "-" + subTask);
    }

    public int get(int index, String subTask, int def) {
        return subTaskData.getOrDefault(index + "-" + subTask, def);
    }
}
