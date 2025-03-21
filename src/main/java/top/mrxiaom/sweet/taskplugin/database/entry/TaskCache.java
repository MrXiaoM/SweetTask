package top.mrxiaom.sweet.taskplugin.database.entry;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class TaskCache {
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

    public void put(int index, String subTask, int data) {
        put(index + "-" + subTask, data);
    }

    public void put(TaskWrapper wrapper, int data) {
        put(wrapper.index, wrapper.subTask.type(), data);
    }

    @Nullable
    public Integer get(int index, String subTask) {
        return subTaskData.get(index + "-" + subTask);
    }

    @Nullable
    public Integer get(TaskWrapper wrapper) {
        return get(wrapper.index, wrapper.subTask.type());
    }

    public int get(int index, String subTask, int def) {
        return subTaskData.getOrDefault(index + "-" + subTask, def);
    }

    public int get(TaskWrapper wrapper, int def) {
        return get(wrapper.index, wrapper.subTask.type(), def);
    }

    public boolean hasDone() {
        Integer i = subTaskData.get(taskId);
        return i != null && i == 1;
    }

    public boolean checkDone(TaskWrapper wrapper) {
        return checkDone(wrapper.task);
    }

    public boolean checkDone(LoadedTask task) {
        if (!task.id.equals(taskId)) return false;
        boolean taskDone = true;
        for (int i = 0; i < task.subTasks.size(); i++) {
            ITask subTask = task.subTasks.get(i);
            String taskType = subTask.type();
            Integer value = get(i, taskType);
            if (value == null) {
                put(i, taskType, 0);
                value = 0;
            }
            if (value < subTask.getTargetValue()) {
                taskDone = false;
            }
        }
        return taskDone;
    }
}
