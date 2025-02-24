package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

public class VanillaBlockMatcher implements BlockMatcher {
    private final Material material;
    private final Integer dataValue;

    public VanillaBlockMatcher(Material material, Integer dataValue) {
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
    public boolean match(Block block) {
        if (dataValue == null) {
            return material.equals(block.getType());
        }
        return material.equals(block.getType()) && dataValue.byteValue() == block.getData();
    }
}
