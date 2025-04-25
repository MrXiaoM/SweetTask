package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

public interface ItemMatcher {

    boolean match(ItemStack item);

    @Nullable
    static ItemMatcher of(String s) {
        if (s.equalsIgnoreCase("ANY")) return AnyItemMatcher.INSTANCE;
        if (s.startsWith("mythic:")) {
            return new MythicItemMatcher(s.substring(7));
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s.toUpperCase());
        if (pair != null) {
            return new VanillaItemMatcher(pair.getKey(), pair.getValue());
        }
        return null;
    }
}
