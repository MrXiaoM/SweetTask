package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;

public class PluginBaseIcon implements IconProvider {
    private final LoadedIcon icon;
    public PluginBaseIcon(LoadedIcon icon) {
        this.icon = icon;
    }
    @Nullable
    @Override
    public ItemStack create() {
        return icon.generateIcon(null);
    }
}
