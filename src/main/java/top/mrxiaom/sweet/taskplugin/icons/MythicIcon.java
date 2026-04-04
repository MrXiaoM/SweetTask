package top.mrxiaom.sweet.taskplugin.icons;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

public class MythicIcon implements IconProvider {
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
    public ItemStack create() {
        IMythic mythic = plugin.getMythic();
        if (mythic == null) {
            return null;
        }
        return mythic.getItem(id);
    }
}
