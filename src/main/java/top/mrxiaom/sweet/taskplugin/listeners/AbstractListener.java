package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrappers;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractListener<E, T> extends AbstractModule implements Listener {
    protected final List<TaskWrappers<T>> wrappers = new ArrayList<>();
    private final Map<EnumTaskType, String> doneTips = new HashMap<>();
    protected boolean disableReverseListener;
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
        disableReverseListener = !config.getBoolean("enable-reverse-listener");
        doneTips.clear();
        for (EnumTaskType value : EnumTaskType.values()) {
            String msg = config.getString("done-tips." + value.name().toLowerCase().replace("_", "-"), null);
            if (msg != null) {
                doneTips.put(value, msg);
            }
        }
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
                int old = taskCache.get(wrapper, 0);
                if (old >= max) continue; // 已经满了的子任务进度不提示消息
                // 增加后的数值
                int data = strict(old + add, 0, max);
                taskCache.put(wrapper, data);
                if (!changed) {
                    // 只打印第一条 Action 消息
                    TaskManager.inst().showActionTips(player, wrapper, data);
                }
                changed = true;
                // 如果数据增加后，任务完成了
                if (taskCache.checkDone(wrapper)) {
                    String msg = wrapper.task.overrideDoneTips;
                    if (msg == null) {
                        msg = doneTips.get(wrapper.task.type);
                    }
                    if (msg != null && !msg.isEmpty()) {
                        String parsed = PAPI.setPlaceholders(player, msg.replace("%name%", wrapper.task.name));
                        AdventureUtil.sendMessage(player, parsed);
                    }
                }
            }
        }
        // 如果有改动，计划在 30 秒后提交数据
        if (changed /*&& taskCollection != null*/) {
            taskCollection.scheduleSubmit(30);
        }
    }

    @SuppressWarnings({"ManualMinMaxCalculation", "SameParameterValue"})
    private static int strict(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
