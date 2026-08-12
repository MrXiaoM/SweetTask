package top.mrxiaom.sweet.taskplugin.matchers.item;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

public class AnyItemMatcher implements ItemMatcher {
    public static final AnyItemMatcher INSTANCE = new AnyItemMatcher();
    private AnyItemMatcher() {}
    @Override
    public boolean match(ItemStack block) {
        return true;
    }
}
