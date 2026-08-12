package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.matchers.item.AnyItemMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.item.CraftEngineItemMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.item.MythicItemMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.item.VanillaItemMatcher;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

public interface ItemMatcher {

    boolean match(ItemStack item);

    @Nullable
    static ItemMatcher of(String s) {
        String lower = s.toLowerCase();
        if (lower.equals("any")) {
            return AnyItemMatcher.INSTANCE;
        }
        IMythic mythic = SweetTask.getInstance().getMythic();
        if (lower.startsWith("craft-engine:")) {
            return new CraftEngineItemMatcher(s.substring(13));
        }
        if (lower.startsWith("mythic:") && mythic != null) {
            return new MythicItemMatcher(mythic, s.substring(7));
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s.toUpperCase());
        if (pair != null) {
            return new VanillaItemMatcher(pair.getKey(), pair.getValue());
        }
        return null;
    }
}
