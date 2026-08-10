package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

public class VanillaAgeableBlockMatcher implements BlockMatcher {
    private final Material material;
    private final Integer dataValue;
    private final int minAge;

    public VanillaAgeableBlockMatcher(Material material, Integer dataValue, int minAge) {
        this.material = material;
        this.dataValue = dataValue;
        this.minAge = minAge;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getDataValue() {
        return dataValue;
    }

    @Override
    public boolean match(Block block) {
        if (isMaterialMatch(block)) {
            BlockData data = block.getBlockData();
            if (data instanceof Ageable) {
                int age = ((Ageable) data).getAge();
                if (minAge == -1) {
                    int maxAge = ((Ageable) data).getMaximumAge();
                    return age >= maxAge;
                } else {
                    return age >= minAge;
                }
            }
        }
        return false;
    }

    @SuppressWarnings({"deprecation"})
    public boolean isMaterialMatch(Block block) {
        if (dataValue == null) {
            return material.equals(block.getType());
        }
        return material.equals(block.getType()) && dataValue.byteValue() == block.getData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaAgeableBlockMatcher)) return false;
        VanillaAgeableBlockMatcher that = (VanillaAgeableBlockMatcher) o;
        return material == that.material && Objects.equals(dataValue, that.dataValue) && Objects.equals(minAge, that.minAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, dataValue);
    }
}
