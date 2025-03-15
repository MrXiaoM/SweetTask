package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IconProvider {
    @Nullable ItemStack create();
}
