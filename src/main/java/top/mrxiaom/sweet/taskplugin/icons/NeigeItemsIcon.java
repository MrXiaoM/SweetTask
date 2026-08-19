package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class NeigeItemsIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        String str = config.getString(key, null);
        if (str != null) {
            String lower = str.toLowerCase();
            if (lower.startsWith("ni:")) {
                String id = str.substring(3);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neigeitem:")) {
                String id = str.substring(10);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neige-item:")) {
                String id = str.substring(11);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neigeitems:")) {
                String id = str.substring(11);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neige-items:")) {
                String id = str.substring(12);
                return new NeigeItemsIcon(id);
            }
        }
        return null;
    };
    private final String id;
    public NeigeItemsIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return ItemManager.INSTANCE.getItemStack(id, player);
    }
}
