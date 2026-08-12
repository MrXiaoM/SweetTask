package top.mrxiaom.sweet.taskplugin.matchers.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Objects;

public class NeigeItemsItemMatcher implements ItemMatcher {
    private final String itemId;
    public NeigeItemsItemMatcher(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        String itemId = ItemManager.INSTANCE.getItemId(item);
        return this.itemId.equals(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeigeItemsItemMatcher)) return false;
        NeigeItemsItemMatcher that = (NeigeItemsItemMatcher) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
