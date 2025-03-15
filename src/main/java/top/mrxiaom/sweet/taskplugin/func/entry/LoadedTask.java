package top.mrxiaom.sweet.taskplugin.func.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class LoadedTask {
    public final String id;
    public final EnumTaskType type;
    public final double rarity;
    public final String name;
    public final List<String> description;
    public final List<ITask> subTasks;
    public final List<IAction> rewards;
    public final List<String> rewardsLore;

    public LoadedTask(String id, EnumTaskType type, double rarity, String name, List<String> description, List<ITask> subTasks, List<IAction> rewards, List<String> rewardsLore) {
        this.id = id;
        this.type = type;
        this.rarity = rarity;
        this.name = name;
        this.description = description;
        this.subTasks = subTasks;
        this.rewards = rewards;
        this.rewardsLore = rewardsLore;
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
        EnumTaskType type = Util.valueOr(EnumTaskType.class, config.getString("type"), null);
        if (type == null) {
            parent.warn("[tasks/" + id + "] 任务类型输入有误");
            return null;
        }
        List<String> description = config.getStringList("description");
        List<ITask> subTasks = new ArrayList<>();
        for (String s : config.getStringList("sub-tasks")) {
            ITask task = ITask.load(parent, id, s);
            if (task != null) {
                subTasks.add(task);
            }
        }
        List<IAction> rewards = loadActions(config, "rewards");
        List<String> rewardsLore = config.getStringList("rewards-lore");
        return new LoadedTask(id, type, rarity, name, description, subTasks, rewards, rewardsLore);
    }
}
