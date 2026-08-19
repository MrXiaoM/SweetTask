package top.mrxiaom.sweet.taskplugin.matchers.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

public class AnyItemMatcher implements ItemMatcher {
    public static final AnyItemMatcher INSTANCE = new AnyItemMatcher();
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable ItemMatcher parse(@NotNull String input) {
            if (input.equalsIgnoreCase("any")) {
                return INSTANCE;
            }
            return null;
        }
        @Override
        public int getPriority() {
            return 0;
        }
    };
    private AnyItemMatcher() {}
    @Override
    public boolean match(ItemStack block) {
        return true;
    }
}
