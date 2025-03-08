package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractListener<E, T> extends AbstractModule implements Listener {
    protected final List<TaskWrappers<T>> wrappers = new ArrayList<>();
    public AbstractListener(SweetTask plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public int priority() {
        return 1001;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        wrappers.clear();
        TaskManager manager = TaskManager.inst();
        Map<T, List<TaskWrapper>> map = new HashMap<>();
        for (LoadedTask task : manager.getTasks()) {
            for (int i = 0; i < task.subTasks.size(); i++) {
                ITask subTask = task.subTasks.get(i);
                handleLoadTask(map, task, subTask, i);
            }
        }
        for (Map.Entry<T, List<TaskWrapper>> entry : map.entrySet()) {
            T matcher = entry.getKey();
            List<TaskWrapper> subTasks = entry.getValue();
            wrappers.add(new TaskWrappers<>(matcher, subTasks));
        }
    }

    protected abstract void handleLoadTask(Map<T, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index);
    protected abstract boolean isNotMatch(T matcher, E entry);
    protected void plus(Player player, E entry, int add) {
        PlayerCache taskCollection = null;
        boolean changed = false;
        // 遍历所有满足条件的 wrapper
        for (TaskWrappers<T> value : wrappers) {
            if (isNotMatch(value.matcher, entry)) continue;
            if (taskCollection == null) {
                // 满足条件了才拉取缓存数据
                taskCollection = plugin.getDatabase().getTasks(player);
            }
            // 对所有满足条件的子任务数据 进行增加
            for (TaskWrapper wrapper : value.subTasks) {
                TaskCache taskCache = taskCollection.tasks.get(wrapper.task.id);
                int max = wrapper.subTask.getTargetValue();
                // 增加后的数值
                int data = Math.min(taskCache.get(wrapper, 0) + add, max);
                taskCache.put(wrapper, data);
                if (!changed) {
                    // 只打印第一条 Action 消息
                    TaskManager.inst().showActionTips(player, wrapper, data);
                }
                changed = true;
            }
        }
        // 如果有改动，计划在 30 秒后提交数据
        if (changed /*&& taskCollection != null*/) {
            taskCollection.scheduleSubmit(30);
        }
    }

}
