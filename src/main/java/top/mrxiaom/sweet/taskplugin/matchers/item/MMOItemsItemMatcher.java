package top.mrxiaom.sweet.taskplugin.matchers.item;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.List;
import java.util.Objects;

public class MMOItemsItemMatcher implements ItemMatcher {
    public static final Provider PROVIDER = (input) -> {
        String lower = input.toLowerCase();
        if (lower.startsWith("mmoitems:")) {
            List<String> split = CollectionUtils.split(input.substring(9), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        if (lower.startsWith("mmoitem:")) {
            List<String> split = CollectionUtils.split(input.substring(8), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        if (lower.startsWith("mi:")) {
            List<String> split = CollectionUtils.split(input.substring(3), ':', 2);
            if (split.size() == 2) {
                return new MMOItemsItemMatcher(split.get(0), split.get(1));
            }
        }
        return null;
    };
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
