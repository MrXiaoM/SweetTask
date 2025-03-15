package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;

public class VanillaIcon implements IconProvider {
    private final Pair<Material, Integer> pair;
    public VanillaIcon(Pair<Material, Integer> pair) {
        this.pair = pair;
    }
    @Nullable
    @Override
    public ItemStack create() {
        return ItemStackUtil.legacy(pair);
    }
}
