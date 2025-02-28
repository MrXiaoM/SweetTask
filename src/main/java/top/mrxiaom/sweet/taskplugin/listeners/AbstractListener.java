package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Listener;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractListener<T> extends AbstractModule implements Listener {
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
}
