package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class VanillaItemMatcher implements ItemMatcher{
    private final Material material;
    private final Integer dataValue;

    public VanillaItemMatcher(Material material, Integer dataValue) {
        this.material = material;
        this.dataValue = dataValue;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getDataValue() {
        return dataValue;
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        if (dataValue == null) {
            return material.equals(item.getType());
        }
        return material.equals(item.getType()) && dataValue.shortValue() == item.getDurability();
    }
}
