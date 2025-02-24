package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

public interface BlockMatcher {

    boolean match(Block block);

    @Nullable
    static BlockMatcher of(String s) {
        if (s.equalsIgnoreCase("ANY")) return AnyBlockMatcher.INSTANCE;
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s);
        if (pair == null) return null;
        return new VanillaBlockMatcher(pair.getKey(), pair.getValue());
    }
}
