package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.depend.IA;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.gui.TaskIcon;

public class ItemsAdderIcon implements PluginIcon {
    public static final Provider PROVIDER = (plugin, config, key) -> {
        String str = config.getString(key, null);
        if (str != null) {
            if (str.startsWith("ia-")) {
                String id = str.substring(3);
                return new ItemsAdderIcon(id);
            }
            if (str.startsWith("itemsadder-")) {
                String id = str.substring(11);
                return new ItemsAdderIcon(id);
            }
            if (str.startsWith("items-adder-")) {
                String id = str.substring(12);
                return new ItemsAdderIcon(id);
            }
        }
        return null;
    };
    private final String id;
    public ItemsAdderIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@NotNull TaskIcon icon, @NotNull Player player, @NotNull TaskCache cache) {
        return IA.get(id).orElse(null);
    }
}
