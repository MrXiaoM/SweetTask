package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.matchers.item.*;

public interface ItemMatcher {

    boolean match(ItemStack item);

    @Nullable
    static ItemMatcher of(String s) {
        for (Provider provider : SweetTask.getInstance().itemMatchers().all()) {
            ItemMatcher matcher = provider.parse(s);
            if (matcher != null) {
                return matcher;
            }
        }
        return null;
    }

    interface Provider extends WithPriority {
        @Nullable ItemMatcher parse(@NotNull String input);
    }
}
