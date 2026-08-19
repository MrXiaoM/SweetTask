package top.mrxiaom.sweet.taskplugin.matchers.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;

import java.util.List;
import java.util.Objects;

public class VanillaAgeableBlockMatcher implements BlockMatcher {
    public static final Provider PROVIDER = (input) -> {
        String lower = input.toLowerCase();
        if (lower.startsWith("age:")) {
            List<String> split = CollectionUtils.split(input.substring(4), ':');
            if (split.size() == 2) {
                String minAgeStr = split.get(0);
                Integer minAge = minAgeStr.isEmpty() ? Integer.valueOf(-1) : Util.parseInt(minAgeStr).orElse(null);
                Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(split.get(1));
                if (minAge != null && pair != null) {
                    return new VanillaAgeableBlockMatcher(pair.key(), pair.value(), minAge);
                }
            }
        }
        return null;
    };
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
