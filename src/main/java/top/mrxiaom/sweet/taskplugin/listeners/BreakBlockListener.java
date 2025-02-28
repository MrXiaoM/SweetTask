package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.database.entry.SubTaskCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskBreakBlock;

import java.util.*;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class BreakBlockListener extends AbstractListener<BlockMatcher> {
    public BreakBlockListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<BlockMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskBreakBlock) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (BlockMatcher block : ((TaskBreakBlock) subTask).blocks) {
                List<TaskWrapper> list = getListOrEmpty(map, block);
                list.add(wrapper);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        TaskProcessDatabase database = plugin.getDatabase();
        Block block = e.getBlock();
        Player player = e.getPlayer();
        TaskCache taskCache = database.getTasks(player);
        boolean changed = false;
        for (TaskWrappers<BlockMatcher> value : wrappers) {
            if (!value.matcher.match(block)) continue;
            for (TaskWrapper wrapper : value.subTasks) {
                SubTaskCache cache = taskCache.tasks.get(wrapper.task.id);
                int data = cache.get(wrapper, 0) + 1;
                cache.put(wrapper, data);
                if (!changed) {
                    TaskManager.inst().showActionTips(player, wrapper, data);
                }
                changed = true;
            }
        }
        if (changed) {
            taskCache.scheduleSubmit(30);
        }
    }
}
