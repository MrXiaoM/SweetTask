package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskGainExp;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class GainExpListener extends AbstractListener<Player, String> {
    public GainExpListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<String, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskGainExp) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            List<TaskWrapper> list = getListOrEmpty(map, "whatever");
            list.add(wrapper);
        }
    }

    @Override
    protected boolean isNotMatch(String matcher, Player entry) {
        return true;
    }

    @EventHandler
    public void onPlayerGainExp(PlayerExpChangeEvent e) {
        Player player = e.getPlayer();
        int amount = e.getAmount();
        if (amount > 0) {
            plus(player, player, amount);
        }
    }
}
