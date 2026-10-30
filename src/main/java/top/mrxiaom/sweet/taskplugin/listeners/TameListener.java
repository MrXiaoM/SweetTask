package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskTame;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class TameListener extends AbstractListener<LivingEntity, EntityMatcher> {
    public TameListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<EntityMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskTame) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (EntityMatcher entity : ((TaskTame) subTask).entities) {
                List<TaskWrapper> list = getListOrEmpty(map, entity);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(EntityMatcher matcher, LivingEntity entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTameEntity(EntityTameEvent e) {
        if (e.isCancelled()) return;
        AnimalTamer tamer = e.getOwner();
        if (tamer instanceof Player) {
            Player player = (Player) tamer;
            LivingEntity entity = e.getEntity();
            plus(player, entity, 1);
        }
    }
}
