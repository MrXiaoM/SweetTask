package top.mrxiaom.sweet.taskplugin.matchers;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CraftEngineItemMatcher implements ItemMatcher {
    private final Key itemId;
    public CraftEngineItemMatcher(String itemId) {
        this.itemId = Key.of(itemId);
    }

    public Key getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        BukkitItemDefinition customItem = CraftEngineItems.byItemStack(item);
        if (customItem != null) {
            return itemId.equals(customItem.id());
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CraftEngineItemMatcher)) return false;
        CraftEngineItemMatcher that = (CraftEngineItemMatcher) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
