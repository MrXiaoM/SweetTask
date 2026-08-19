package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class PluginBaseIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        if (config.isConfigurationSection(key) && "icon".equalsIgnoreCase(config.getString(key + ".type"))) {
            LoadedIcon icon = LoadedIcon.load(config, key);
            return new PluginBaseIcon(icon);
        }
        return null;
    };
    private final LoadedIcon icon;
    public PluginBaseIcon(LoadedIcon icon) {
        this.icon = icon;
    }
    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return this.icon.generateIcon(player);
    }
}
