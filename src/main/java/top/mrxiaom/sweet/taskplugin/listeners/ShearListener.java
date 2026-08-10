package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskShear;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class ShearListener extends AbstractListener<LivingEntity, EntityMatcher> {
    public ShearListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<EntityMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskShear) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (EntityMatcher entity : ((TaskShear) subTask).entities) {
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
    public void onPlayerShearEntity(PlayerShearEntityEvent e) {
        if (e.isCancelled()) return;
        Entity entity = e.getEntity();
        if (entity instanceof LivingEntity) {
            plus(e.getPlayer(), (LivingEntity) entity, 1);
        }
    }
}
