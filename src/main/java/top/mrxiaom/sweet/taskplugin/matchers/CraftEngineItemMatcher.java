package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.Objects;

public class CraftEngineItemMatcher implements ItemMatcher {
    private final String itemId;
    public CraftEngineItemMatcher(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public boolean match(ItemStack item) {
        return mythicId.equals(mythic.getMythicId(item));
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
