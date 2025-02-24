package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.block.Block;

public class AnyBlockMatcher implements BlockMatcher {
    public static final AnyBlockMatcher INSTANCE = new AnyBlockMatcher();
    private AnyBlockMatcher() {}
    @Override
    public boolean match(Block block) {
        return true;
    }
}
