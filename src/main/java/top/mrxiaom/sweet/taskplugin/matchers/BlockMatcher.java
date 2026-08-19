package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public interface BlockMatcher {

    boolean match(Block block);

    @Nullable
    static BlockMatcher of(String s) {
        for (Provider provider : SweetTask.getInstance().blockMatchers().all()) {
            BlockMatcher matcher = provider.parse(s);
            if (matcher != null) {
                return matcher;
            }
        }
        return null;
    }

    interface Provider extends WithPriority {
        @Nullable BlockMatcher parse(@NotNull String input);
    }
}
