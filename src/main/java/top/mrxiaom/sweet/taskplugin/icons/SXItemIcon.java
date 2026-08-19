package top.mrxiaom.sweet.taskplugin.icons;

import github.saukiya.sxitem.SXItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class SXItemIcon implements PluginIcon {
    public static final Provider PROVIDER = new Provider() {
        @Nullable
        @Override
        public PluginIcon load(@NotNull SweetTask plugin, @NotNull ConfigurationSection config, @NotNull String key) {
            String str = config.getString(key, null);
            if (str != null) {
                return load(plugin, str);
            }
            return null;
        }
        @Nullable
        @Override
        public PluginIcon load(@NotNull SweetTask plugin, @NotNull String input) {
            String lower = input.toLowerCase();
            if (lower.startsWith("si:")) {
                String id = input.substring(3);
                return new SXItemIcon(id);
            }
            if (lower.startsWith("sxitem:")) {
                String id = input.substring(7);
                return new SXItemIcon(id);
            }
            if (lower.startsWith("sx-item:")) {
                String id = input.substring(8);
                return new SXItemIcon(id);
            }
            return null;
        }
    };
    private final String id;
    public SXItemIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        if (player != null) {
            return SXItem.getItemManager().getItem(id, player);
        } else {
            return SXItem.getItemManager().getItem(id);
        }
    }
}
