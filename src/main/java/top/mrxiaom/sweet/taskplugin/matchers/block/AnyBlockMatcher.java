package top.mrxiaom.sweet.taskplugin.matchers.block;

import org.bukkit.block.Block;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;

public class AnyBlockMatcher implements BlockMatcher {
    public static final AnyBlockMatcher INSTANCE = new AnyBlockMatcher();
    private AnyBlockMatcher() {}
    @Override
    public boolean match(Block block) {
        return true;
    }
}
