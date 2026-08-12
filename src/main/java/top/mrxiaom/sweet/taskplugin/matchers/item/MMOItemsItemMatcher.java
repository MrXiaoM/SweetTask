package top.mrxiaom.sweet.taskplugin.matchers.item;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Objects;

public class MMOItemsItemMatcher implements ItemMatcher {
    private final String type;
    private final String itemId;
    public MMOItemsItemMatcher(String type, String itemId) {
        this.type = type;
        this.itemId = itemId;
    }

    public String getType() {
        return type;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        LiveMMOItem mmoItem = new LiveMMOItem(item);
        Type type = mmoItem.getType();
        if (type != null) {
            String itemId = mmoItem.getId();
            return this.type.equals(type.getId()) && this.itemId.equals(itemId);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOItemsItemMatcher)) return false;
        MMOItemsItemMatcher that = (MMOItemsItemMatcher) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
