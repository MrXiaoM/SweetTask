package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskCrafting;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class CraftingListener extends AbstractListener<ItemStack, ItemMatcher> {
    public CraftingListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<ItemMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskCrafting) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (ItemMatcher item : ((TaskCrafting) subTask).items) {
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
    public void onItemCraft(CraftItemEvent e) {
        if (e.isCancelled()) return;
        HumanEntity whoClicked = e.getWhoClicked();
        if (!(whoClicked instanceof Player)) return;
        Player player = (Player) whoClicked;
        ItemStack item = e.getCurrentItem();
        if (item != null) {
            int amount = item.getAmount();
            if (amount > 0) {
                plus(player, item, amount);
            }
        }
    }
}
