package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;

import java.util.Map;

@AutoRegister
public class KillListener extends AbstractModule implements Listener {
    public KillListener(SweetTask plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler
    public void onPlayerKillMob(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        // TODO: 处理任务事件
    }
}
