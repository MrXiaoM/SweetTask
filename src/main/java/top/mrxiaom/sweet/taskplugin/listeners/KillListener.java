package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskKill;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class KillListener extends AbstractListener<LivingEntity, EntityMatcher> {
    public KillListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<EntityMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskKill) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (EntityMatcher entity : ((TaskKill) subTask).entities) {
                List<TaskWrapper> list = getListOrEmpty(map, entity);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(EntityMatcher matcher, LivingEntity entry) {
        return !matcher.match(entry);
    }

    @EventHandler
    public void onPlayerKillMob(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        plus(killer, entity, 1);
    }
}
