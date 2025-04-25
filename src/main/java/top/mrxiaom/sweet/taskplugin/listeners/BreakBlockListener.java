package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskBreakBlock;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class BreakBlockListener extends AbstractListener<Block, BlockMatcher> {
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

    @Override
    protected boolean isNotMatch(BlockMatcher matcher, Block entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        plus(player, block, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled() || disableReverseListener) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        plus(player, block, -1);
    }
}
