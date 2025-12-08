package top.mrxiaom.sweet.taskplugin.func.entry;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.*;

public class TaskTypeInstance {
    private final EnumTaskType taskType;
    private final Map<String, LoadedTask> tasksByType = new HashMap<>();
    private final List<Pair<String, Integer>> maxTaskCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> maxRefreshCounts = new ArrayList<>();
    private int maxTaskCountDefault, maxRefreshCountDefault;

    public TaskTypeInstance(EnumTaskType taskType) {
        this.taskType = taskType;
    }

    @NotNull
    public String key() {
        return taskType.key();
    }

    @NotNull
    public String getDisplayInLog() {
        return taskType.getDisplayInLog();
    }

    @NotNull
    public EnumTaskType type() {
        return taskType;
    }

    @ApiStatus.Internal
    public void clearTasks() {
        tasksByType.clear();
    }

    @ApiStatus.Internal
    public void addTask(LoadedTask task) {
        tasksByType.put(task.id, task);
    }

    @NotNull
    public Map<String, LoadedTask> getTasks() {
        return Collections.unmodifiableMap(tasksByType);
    }

    @NotNull
    @ApiStatus.Internal
    public List<Pair<String, Integer>> getMaxTaskCounts() {
        return maxTaskCounts;
    }

    public int getMaxTaskCountDefault() {
        return maxTaskCountDefault;
    }

    public void setMaxTaskCountDefault(int taskCountDefault) {
        this.maxTaskCountDefault = taskCountDefault;
    }

    @NotNull
    @ApiStatus.Internal
    public List<Pair<String, Integer>> getMaxRefreshCounts() {
        return maxRefreshCounts;
    }

    public int getMaxRefreshCountDefault() {
        return maxRefreshCountDefault;
    }

    public void setMaxRefreshCountDefault(int refreshCountDefault) {
        this.maxRefreshCountDefault = refreshCountDefault;
    }

    public int getMaxTasksCount(@NotNull Permissible player) {
        return getCount(maxTaskCounts, player, maxTaskCountDefault);
    }

    public int getMaxRefreshCount(@NotNull Permissible player) {
        return getCount(maxRefreshCounts, player, maxRefreshCountDefault);
    }

    private int getCount(List<Pair<String, Integer>> permList, Permissible player, int def) {
        for (Pair<String, Integer> pair : permList) {
            if (player.hasPermission(pair.getKey())) {
                return pair.getValue();
            }
        }
        return def;
    }
}
