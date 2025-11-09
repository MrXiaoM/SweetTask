package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskFishing;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class FishingListener extends AbstractListener<ItemStack, ItemMatcher> {
    public FishingListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<ItemMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskFishing) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (ItemMatcher item : ((TaskFishing) subTask).items) {
                List<TaskWrapper> list = getListOrEmpty(map, item);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(ItemMatcher matcher, ItemStack entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishing(PlayerFishEvent e) {
        if (e.isCancelled()) return;
        if (!e.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) return;
        Player player = e.getPlayer();
        if (e.getCaught() instanceof Item) {
            ItemStack item = ((Item) e.getCaught()).getItemStack();
            int amount = item.getAmount();
            if (amount > 0) {
                plus(player, item, amount);
            }
        }
    }
}
