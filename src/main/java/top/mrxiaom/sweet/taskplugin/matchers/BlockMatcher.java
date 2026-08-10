package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.List;
import java.util.Optional;

public interface BlockMatcher {

    boolean match(Block block);

    @Nullable
    static BlockMatcher of(String s) {
        String lower = s.toLowerCase();
        if (lower.equals("any")) {
            return AnyBlockMatcher.INSTANCE;
        }
        if (lower.startsWith("age:")) {
            List<String> split = CollectionUtils.split(s.substring(4), ':');
            if (split.size() == 2) {
                Integer minAge = Util.parseInt(split.get(0)).orElse(null);
                Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(split.get(1));
                if (minAge != null && pair != null) {
                    return new VanillaAgeableBlockMatcher(pair.key(), pair.value(), minAge);
                }
            }
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s);
        if (pair != null) {
            return new VanillaBlockMatcher(pair.key(), pair.value());
        }
        return null;
    }
}
