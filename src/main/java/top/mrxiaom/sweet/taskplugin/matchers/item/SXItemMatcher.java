package top.mrxiaom.sweet.taskplugin.matchers.item;

import github.saukiya.sxitem.SXItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Objects;

public class SXItemMatcher implements ItemMatcher {
    public static final Provider PROVIDER = (input) -> {
        String lower = input.toLowerCase();
        if (lower.startsWith("sx-item:")) {
            return new SXItemMatcher(input.substring(8));
        }
        if (lower.startsWith("sxitem:")) {
            return new SXItemMatcher(input.substring(7));
        }
        if (lower.startsWith("si:")) {
            return new SXItemMatcher(input.substring(3));
        }
        return null;
    };
    private final String itemId;
    public SXItemMatcher(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        String itemId = SXItem.getItemManager().getItemKey(item);
        return this.itemId.equals(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SXItemMatcher)) return false;
        SXItemMatcher that = (SXItemMatcher) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
