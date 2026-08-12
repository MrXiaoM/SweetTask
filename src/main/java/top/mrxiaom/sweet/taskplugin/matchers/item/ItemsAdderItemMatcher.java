package top.mrxiaom.sweet.taskplugin.matchers.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.depend.IA;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Objects;

public class ItemsAdderItemMatcher implements ItemMatcher {
    private final String itemId;
    public ItemsAdderItemMatcher(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        String itemId = IA.getFullId(item).orElse(null);
        return this.itemId.equals(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemsAdderItemMatcher)) return false;
        ItemsAdderItemMatcher that = (ItemsAdderItemMatcher) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
