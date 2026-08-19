package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class NeigeItemsIcon implements PluginIcon {
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
            if (lower.startsWith("ni:")) {
                String id = input.substring(3);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neigeitem:")) {
                String id = input.substring(10);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neige-item:")) {
                String id = input.substring(11);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neigeitems:")) {
                String id = input.substring(11);
                return new NeigeItemsIcon(id);
            }
            if (lower.startsWith("neige-items:")) {
                String id = input.substring(12);
                return new NeigeItemsIcon(id);
            }
            return null;
        }
    };
    private final String id;
    public NeigeItemsIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        if (player != null) {
            return ItemManager.INSTANCE.getItemStack(id, player);
        } else {
            return ItemManager.INSTANCE.getItemStack(id);
        }
    }
}
