package top.mrxiaom.sweet.taskplugin.matchers.enchant;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.matchers.EnchantMatcher;

public class AnyEnchantMatcher implements EnchantMatcher {
    public static final AnyEnchantMatcher INSTANCE = new AnyEnchantMatcher();
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable EnchantMatcher parse(@NotNull String input) {
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
    private AnyEnchantMatcher() {}
    @Override
    public boolean match(ItemStack block) {
        return true;
    }
}
