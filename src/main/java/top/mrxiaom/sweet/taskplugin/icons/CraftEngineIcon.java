package top.mrxiaom.sweet.taskplugin.icons;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class CraftEngineIcon implements PluginIcon {
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
            if (lower.startsWith("ce:")) {
                String id = input.substring(3);
                return new CraftEngineIcon(id);
            }
            if (lower.startsWith("craftengine:")) {
                String id = input.substring(12);
                return new CraftEngineIcon(id);
            }
            if (lower.startsWith("craft-engine:")) {
                String id = input.substring(13);
                return new CraftEngineIcon(id);
            }
            return null;
        }
    };
    private final Key id;
    public CraftEngineIcon(String id) {
        this.id = Key.of(id);
    }

    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        BukkitItemDefinition customItem = CraftEngineItems.byId(id);
        if (customItem != null) {
            if (player != null) {
                return customItem.buildBukkitItem(player);
            } else {
                return customItem.buildBukkitItem();
            }
        }
        return null;
    }
}
