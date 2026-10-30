package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public interface EnchantMatcher {

    boolean match(ItemStack item);

    @Nullable
    static EnchantMatcher of(String s) {
        for (Provider provider : SweetTask.getInstance().enchantMatchers().all()) {
            EnchantMatcher matcher = provider.parse(s);
            if (matcher != null) {
                return matcher;
            }
        }
        return null;
    }

    interface Provider extends WithPriority {
        @Nullable EnchantMatcher parse(@NotNull String input);
    }
}
