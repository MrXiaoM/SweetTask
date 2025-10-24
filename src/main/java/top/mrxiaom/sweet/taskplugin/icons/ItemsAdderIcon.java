package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.depend.IA;

public class ItemsAdderIcon implements IconProvider {
    private final String id;
    public ItemsAdderIcon(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public ItemStack create() {
        return IA.get(id).orElse(null);
    }
}
