package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.matchers.item.*;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.List;

public interface ItemMatcher {

    boolean match(ItemStack item);

    @Nullable
    static ItemMatcher of(String s) {
        String lower = s.toLowerCase();
        if (lower.equals("any")) {
            return AnyItemMatcher.INSTANCE;
        }
        IMythic mythic = SweetTask.getInstance().getMythic();
        // CraftEngine
        if (lower.startsWith("craft-engine:")) {
            return new CraftEngineItemMatcher(s.substring(13));
        }
        if (lower.startsWith("craftengine:")) {
            return new CraftEngineItemMatcher(s.substring(12));
        }
        if (lower.startsWith("ce:")) {
            return new CraftEngineItemMatcher(s.substring(3));
        }
        // ItemsAdder
        if (lower.startsWith("items-adder:")) {
            return new ItemsAdderItemMatcher(s.substring(12));
        }
        if (lower.startsWith("itemsadder:")) {
            return new ItemsAdderItemMatcher(s.substring(11));
        }
        if (lower.startsWith("ia:")) {
            return new ItemsAdderItemMatcher(s.substring(3));
        }
        // MMOItems
        if (lower.startsWith("mmoitems:")) {
            List<String> split = CollectionUtils.split(s.substring(9), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        if (lower.startsWith("mmoitem:")) {
            List<String> split = CollectionUtils.split(s.substring(8), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        if (lower.startsWith("mi:")) {
            List<String> split = CollectionUtils.split(s.substring(3), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        // SX-Item
        if (lower.startsWith("sx-item:")) {
            return new SXItemMatcher(s.substring(8));
        }
        if (lower.startsWith("sxitem:")) {
            return new SXItemMatcher(s.substring(7));
        }
        if (lower.startsWith("si:")) {
            return new SXItemMatcher(s.substring(3));
        }
        // NeigeItems
        if (lower.startsWith("neige-items:")) {
            return new NeigeItemsItemMatcher(s.substring(12));
        }
        if (lower.startsWith("neige-item:")) {
            return new NeigeItemsItemMatcher(s.substring(11));
        }
        if (lower.startsWith("neigeitems:")) {
            return new NeigeItemsItemMatcher(s.substring(10));
        }
        if (lower.startsWith("neigeitem:")) {
            return new NeigeItemsItemMatcher(s.substring(9));
        }
        if (lower.startsWith("ni:")) {
            return new NeigeItemsItemMatcher(s.substring(3));
        }
        // MythicMobs
        if (lower.startsWith("mythic:") && mythic != null) {
            return new MythicItemMatcher(mythic, s.substring(7));
        }
        if (lower.startsWith("mm:") && mythic != null) {
            return new MythicItemMatcher(mythic, s.substring(3));
        }
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(s.toUpperCase());
        if (pair != null) {
            return new VanillaItemMatcher(pair.getKey(), pair.getValue());
        }
        return null;
    }
}
