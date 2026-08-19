package top.mrxiaom.sweet.taskplugin.matchers.block;

import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

public class AnyBlockMatcher implements BlockMatcher {
    public static final AnyBlockMatcher INSTANCE = new AnyBlockMatcher();
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable BlockMatcher parse(@NonNull String input) {
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
    private AnyBlockMatcher() {}
    @Override
    public boolean match(Block block) {
        return true;
    }
}
