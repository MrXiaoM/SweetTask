package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;

@AutoRegister
public class BreakBlockListener extends AbstractModule implements Listener {
    public BreakBlockListener(SweetTask plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        // TODO: 处理任务事件
    }
}
