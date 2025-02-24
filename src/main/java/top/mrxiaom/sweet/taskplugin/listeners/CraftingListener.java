package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;

@AutoRegister
public class CraftingListener extends AbstractModule implements Listener {
    public CraftingListener(SweetTask plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(CraftItemEvent e) {
        if (e.isCancelled()) return;
        // TODO: 处理任务事件
    }
}
