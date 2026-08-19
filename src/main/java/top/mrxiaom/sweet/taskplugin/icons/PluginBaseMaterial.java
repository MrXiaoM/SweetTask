package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.material.IMaterial;

public class PluginBaseMaterial implements IMaterial {
    private final PluginIcon icon;
    public PluginBaseMaterial(PluginIcon icon) {
        this.icon = icon;
    }

    @Override
    public @NotNull ItemStack create(@Nullable Player player, int amount) {
        ItemStack itemStack = icon.create(player);
        if (itemStack != null) {
            itemStack.setAmount(amount);
            return itemStack;
        }
        return new ItemStack(Material.PAPER);
    }
}
