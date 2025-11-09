package top.mrxiaom.sweet.taskplugin.listeners;

import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.matchers.FishMatcher;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskCustomFishing;
import top.mrxiaom.sweet.taskplugin.tasks.TaskFishing;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister(requirePlugins = "CustomFishing")
public class CustomFishingListener extends AbstractListener<Context<Player>, FishMatcher> {
    public CustomFishingListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<FishMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskCustomFishing) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (FishMatcher item : ((TaskCustomFishing) subTask).items) {
                List<TaskWrapper> list = getListOrEmpty(map, item);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(FishMatcher matcher, Context<Player> entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishing(FishingLootSpawnEvent e) {
        Player player = e.getPlayer();
        if (e.getEntity() instanceof Item) {
            ItemStack item = ((Item) e.getEntity()).getItemStack();
            int amount = item.getAmount();
            if (amount > 0) {
                plus(player, e.getContext(), amount);
            }
        }
    }
}
