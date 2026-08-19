package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public interface PluginIcon {
    @Nullable ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache);

    interface Provider extends WithPriority {
        @Nullable PluginIcon load(@NotNull SweetTask plugin, @NotNull ConfigurationSection config, @NotNull String key);
    }
}
