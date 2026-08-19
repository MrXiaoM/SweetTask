package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class VanillaIcon implements PluginIcon {
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
            Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(input);
            if (pair != null) {
                return new VanillaIcon(pair);
            }
            return null;
        }
    };
    private final Pair<Material, Integer> pair;
    public VanillaIcon(Pair<Material, Integer> pair) {
        this.pair = pair;
    }
    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        return ItemStackUtil.legacy(pair);
    }
}
