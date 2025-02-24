package top.mrxiaom.sweet.taskplugin.matchers;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
}
