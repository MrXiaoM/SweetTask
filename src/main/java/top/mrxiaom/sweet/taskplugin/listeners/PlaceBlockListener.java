package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskPlaceBlock;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class PlaceBlockListener extends AbstractListener<Block, BlockMatcher> {
    public PlaceBlockListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<BlockMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskPlaceBlock) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (BlockMatcher block : ((TaskPlaceBlock) subTask).blocks) {
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
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        plus(player, block, 1);
    }
}
