package top.mrxiaom.sweet.taskplugin.matchers.item;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Objects;

public class CraftEngineItemMatcher implements ItemMatcher {
    public static final Provider PROVIDER = (input) -> {
        String lower = input.toLowerCase();
        if (lower.startsWith("craft-engine:")) {
            return new CraftEngineItemMatcher(input.substring(13));
        }
        if (lower.startsWith("craftengine:")) {
            return new CraftEngineItemMatcher(input.substring(12));
        }
        if (lower.startsWith("ce:")) {
            return new CraftEngineItemMatcher(input.substring(3));
        }
        return null;
    };
    private final Key itemId;
    public CraftEngineItemMatcher(String itemId) {
        this.itemId = Key.of(itemId);
    }

    public Key getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
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
