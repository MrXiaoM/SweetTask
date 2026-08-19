package top.mrxiaom.sweet.taskplugin.icons;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class CraftEngineIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        String str = config.getString(key, null);
        if (str != null) {
            String lower = str.toLowerCase();
            if (lower.startsWith("ce:")) {
                String id = str.substring(3);
                return new CraftEngineIcon(id);
            }
            if (lower.startsWith("craftengine:")) {
                String id = str.substring(12);
                return new CraftEngineIcon(id);
            }
            if (lower.startsWith("craft-engine:")) {
                String id = str.substring(13);
                return new CraftEngineIcon(id);
            }
        }
        return null;
    };
    private final Key id;
    public CraftEngineIcon(String id) {
        this.id = Key.of(id);
    }

    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        BukkitItemDefinition customItem = CraftEngineItems.byId(id);
        if (customItem != null) {
            return customItem.buildBukkitItem(player);
        }
        return null;
    }
}
