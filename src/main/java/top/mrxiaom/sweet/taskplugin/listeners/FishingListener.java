package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;

@AutoRegister
public class FishingListener extends AbstractModule implements Listener {
    public FishingListener(SweetTask plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishing(PlayerFishEvent e) {
        if (e.isCancelled()) return;
        // TODO: 处理任务事件
    }
}
