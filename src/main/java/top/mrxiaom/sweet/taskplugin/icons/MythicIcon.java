package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

public class MythicIcon implements PluginIcon {
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
            if (lower.startsWith("mythic-") || lower.startsWith("mythic:")) {
                String id = input.substring(7);
                return new MythicIcon(plugin, id);

            }
            return null;
        }
    };
    private final SweetTask plugin;
    private final String id;

    public MythicIcon(SweetTask plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        if (plugin.getMythic() == null) {
            plugin.warn("加载了 MythicMobs 物品图标 " + id + "，但当前服务端未安装或不支持 MythicMobs");
        }
    }

    @Nullable
    @Override
    public ItemStack create(@Nullable Player player) {
        IMythic mythic = plugin.getMythic();
        if (mythic == null) {
            return null;
        }
        return mythic.getItem(id);
    }
}
