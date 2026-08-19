package top.mrxiaom.sweet.taskplugin.icons;

import github.saukiya.sxitem.SXItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class SXItemIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        String str = config.getString(key, null);
        if (str != null) {
            String lower = str.toLowerCase();
            if (lower.startsWith("si:")) {
                String id = str.substring(3);
                return new SXItemIcon(id);
            }
            if (lower.startsWith("sxitem:")) {
                String id = str.substring(7);
                return new SXItemIcon(id);
            }
            if (lower.startsWith("sx-item:")) {
                String id = str.substring(8);
                return new SXItemIcon(id);
            }
        }
        return null;
    };
    private final String id;
    public SXItemIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return SXItem.getItemManager().getItem(id, player);
    }
}
