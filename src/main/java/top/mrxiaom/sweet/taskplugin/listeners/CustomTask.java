package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskCustom;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class CustomTask extends AbstractListener<String, String> {
    public CustomTask(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<String, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskCustom) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            String key = ((TaskCustom) subTask).key;
            List<TaskWrapper> list = getListOrEmpty(map, key);
            list.add(wrapper);
        }
    }

    @Override
    protected boolean isNotMatch(String matcher, String entry) {
        return !matcher.equals(entry);
    }

    /**
     * 修改某个指定 key 的 custom 子任务数值进度
     * @param player 玩家
     * @param key 键
     * @param modify 修改值，大于<code>0</code>增加，小于<code>0</code>减小
     */
    public void plus(Player player, String key, int modify) {
        super.plus(player, key, modify);
    }

    public static CustomTask inst() {
        return instanceOf(CustomTask.class);
    }
}
