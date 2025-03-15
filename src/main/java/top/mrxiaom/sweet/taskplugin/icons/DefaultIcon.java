package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DefaultIcon implements IconProvider {
    public static final DefaultIcon INSTANCE = new DefaultIcon();
    private DefaultIcon() {}
    @Nullable
    @Override
    public ItemStack create() {
        return new ItemStack(Material.PAPER);
    }
}
