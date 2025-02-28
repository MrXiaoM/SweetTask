package top.mrxiaom.sweet.taskplugin.matchers;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MythicItemMatcher implements ItemMatcher {
    private final String mythicId;
    public MythicItemMatcher(String mythicId) {
        this.mythicId = mythicId;
    }

    public String getMythicId() {
        return mythicId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return mythicId.equals(nbt.getString("MYTHIC_TYPE"));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MythicItemMatcher)) return false;
        MythicItemMatcher that = (MythicItemMatcher) o;
        return Objects.equals(mythicId, that.mythicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mythicId);
    }
}
