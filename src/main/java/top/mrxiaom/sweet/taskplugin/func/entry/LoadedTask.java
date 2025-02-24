package top.mrxiaom.sweet.taskplugin.func.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;

public class LoadedTask {
    public final String id;
    public final double rarity;
    public final String name;
    public final List<ITask> subTasks;
    public final List<IAction> rewards;

    public LoadedTask(String id, double rarity, String name, List<ITask> subTasks, List<IAction> rewards) {
        this.id = id;
        this.rarity = rarity;
        this.name = name;
        this.subTasks = subTasks;
        this.rewards = rewards;
    }


    public void giveRewards(Player player) {
        for (IAction reward : rewards) {
            reward.run(player);
        }
    }

    @Nullable
    public static LoadedTask load(TaskManager parent, ConfigurationSection config, String id) {
        double rarity = config.getDouble("rarity", 0.0) / 100.0;
        if (rarity <= 0) {
            parent.warn("[tasks/" + id + "] 任务稀有度 rarity 的数值有误");
            return null;
        }
        String name = config.getString("name", id);
        List<ITask> subTasks = new ArrayList<>();
        for (String s : config.getStringList("sub-tasks")) {
            ITask task = ITask.load(parent, id, s);
            if (task != null) {
                subTasks.add(task);
            }
        }
        List<IAction> rewards = loadActions(config, "rewards");
        return new LoadedTask(id, rarity, name, subTasks, rewards);
    }
}
