package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskCrafting;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class ConsumeListener extends AbstractListener<ItemStack, ItemMatcher> {
    public ConsumeListener(SweetTask plugin) {
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
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (!isEmpty(item)) {
            int amount = item.getAmount();
            if (amount > 0) {
                plus(player, item, amount);
            }
        }
    }

    @Contract("null -> true")
    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
