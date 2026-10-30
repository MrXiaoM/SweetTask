package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.EnchantMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskEnchant;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class EnchantListener extends AbstractListener<EnchantItemEvent, EnchantListener.EnchantItemMatcher> {
    public EnchantListener(SweetTask plugin) {
        super(plugin);
    }

    public static class EnchantItemMatcher {
        public final ItemMatcher item;
        public final List<EnchantMatcher> enchants;
        public EnchantItemMatcher(ItemMatcher item, List<EnchantMatcher> enchants) {
            this.item = item;
            this.enchants = enchants;
        }

        public boolean match(EnchantItemEvent e) {
            ItemStack item = e.getItem().clone();
            for (Enchantment enchant : item.getEnchantments().keySet()) {
                item.removeEnchantment(enchant);
            }
            item.addUnsafeEnchantments(e.getEnchantsToAdd());
            if (this.item.match(item)) {
                for (EnchantMatcher enchant : enchants) {
                    if (enchant.match(item)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EnchantItemMatcher)) return false;
            EnchantItemMatcher matcher = (EnchantItemMatcher) o;
            return Objects.equals(item, matcher.item) && Objects.equals(enchants, matcher.enchants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, enchants);
        }
    }

    @Override
    protected void handleLoadTask(Map<EnchantItemMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskEnchant) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (ItemMatcher item : ((TaskEnchant) subTask).items) {
                EnchantItemMatcher matcher = new EnchantItemMatcher(item, ((TaskEnchant) subTask).enchants);
                List<TaskWrapper> list = getListOrEmpty(map, matcher);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(EnchantItemMatcher matcher, EnchantItemEvent entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemEnchant(EnchantItemEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getEnchanter();
        plus(player, e, 1);
    }
}
