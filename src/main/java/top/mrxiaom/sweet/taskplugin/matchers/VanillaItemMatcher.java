package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaItemMatcher)) return false;
        VanillaItemMatcher that = (VanillaItemMatcher) o;
        return material == that.material && Objects.equals(dataValue, that.dataValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, dataValue);
    }
}
