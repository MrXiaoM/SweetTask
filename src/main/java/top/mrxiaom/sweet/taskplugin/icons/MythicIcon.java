package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public class MythicIcon implements IconProvider {
    private final SweetTask plugin;
    private final String id;

    public MythicIcon(SweetTask plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create() {
        return plugin.getMythic().getItem(id);
    }
}
