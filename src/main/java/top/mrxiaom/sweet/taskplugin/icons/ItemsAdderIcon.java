package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.depend.IA;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class ItemsAdderIcon implements PluginIcon {
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
            if (lower.startsWith("ia-") || lower.startsWith("ia:")) {
                String id = input.substring(3);
                return new ItemsAdderIcon(id);
            }
            if (lower.startsWith("itemsadder-") || lower.startsWith("itemsadder:")) {
                String id = input.substring(11);
                return new ItemsAdderIcon(id);
            }
            if (lower.startsWith("items-adder-") || lower.startsWith("items-adder:")) {
                String id = input.substring(12);
                return new ItemsAdderIcon(id);
            }
            return null;
        }
    };
    private final String id;
    public ItemsAdderIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        return IA.get(id).orElse(null);
    }
}
