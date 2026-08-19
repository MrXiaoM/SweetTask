package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class VanillaIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        String str = config.getString(key, null);
        if (str != null) {
            Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(str);
            if (pair != null) {
                return new VanillaIcon(pair);
            }
        }
        return null;
    };
    private final Pair<Material, Integer> pair;
    public VanillaIcon(Pair<Material, Integer> pair) {
        this.pair = pair;
    }
    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return ItemStackUtil.legacy(pair);
    }
}
