package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.block.AnyBlockMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.block.CraftEngineBlockMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.block.VanillaAgeableBlockMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.block.VanillaBlockMatcher;

import java.util.List;

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
        if (lower.startsWith("craft-engine:")) {
            return new CraftEngineBlockMatcher(s.substring(13));
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s);
        if (pair != null) {
            return new VanillaBlockMatcher(pair.key(), pair.value());
        }
        return null;
    }
}
