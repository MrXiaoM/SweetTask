package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class DefaultIcon implements PluginIcon {
    public static final DefaultIcon INSTANCE = new DefaultIcon();
    private DefaultIcon() {}
    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return new ItemStack(Material.PAPER);
    }
}
