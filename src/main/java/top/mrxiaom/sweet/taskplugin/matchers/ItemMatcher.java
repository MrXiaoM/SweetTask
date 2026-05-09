package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

public interface ItemMatcher {

    boolean match(ItemStack item);

    @Nullable
    static ItemMatcher of(String s) {
        if (s.equalsIgnoreCase("ANY")) return AnyItemMatcher.INSTANCE;
        IMythic mythic = SweetTask.getInstance().getMythic();
        if (s.startsWith("mythic:") && mythic != null) {
            return new MythicItemMatcher(mythic, s.substring(7));
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s.toUpperCase());
        if (pair != null) {
            return new VanillaItemMatcher(pair.getKey(), pair.getValue());
        }
        return null;
    }
}
