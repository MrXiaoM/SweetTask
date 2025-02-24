package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.inventory.ItemStack;

public class AnyItemMatcher implements ItemMatcher {
    public static final AnyItemMatcher INSTANCE = new AnyItemMatcher();
    private AnyItemMatcher() {}
    @Override
    public boolean match(ItemStack block) {
        return true;
    }
}
