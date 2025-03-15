package top.mrxiaom.sweet.taskplugin.func.entry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.icons.*;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class LoadedTask {
    public final String id;
    public final EnumTaskType type;
    public final double rarity;
    public final IconProvider iconNormal;
    public final IconProvider iconDone;
    public final String name;
    public final List<String> description;
    public final List<ITask> subTasks;
    public final List<IAction> rewards;
    public final List<String> rewardsLore;

    public LoadedTask(String id, EnumTaskType type, double rarity, IconProvider iconNormal, IconProvider iconDone, String name, List<String> description, List<ITask> subTasks, List<IAction> rewards, List<String> rewardsLore) {
        this.id = id;
        this.type = type;
        this.rarity = rarity;
        this.iconNormal = iconNormal;
        this.iconDone = iconDone;
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

    @NotNull
    public ItemStack getIcon(boolean done) {
        ItemStack item = (done ? iconDone : iconNormal).create();
        return item != null ? item : new ItemStack(Material.PAPER);
    }

    @Nullable
    public static LoadedTask load(TaskManager parent, ConfigurationSection config, String id) {
        double rarity = config.getDouble("rarity", 0.0) / 100.0;
        if (rarity <= 0) {
            parent.warn("[tasks/" + id + "] 任务稀有度 rarity 的数值有误");
            return null;
        }
        IconProvider iconNormal = getIcon(parent.plugin, config, "icon.normal");
        IconProvider iconDone = getIcon(parent.plugin, config, "icon.done");
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
        return new LoadedTask(id, type, rarity, iconNormal, iconDone, name, description, subTasks, rewards, rewardsLore);
    }

    private static IconProvider getIcon(SweetTask plugin, ConfigurationSection config, String key) {
        String str = config.getString(key, null);
        if (str != null) {
            if (str.startsWith("mythic-")) {
                String id = str.substring(7);
                return new MythicIcon(plugin, id);
            }
            if (str.startsWith("ia-")) {
                String id = str.substring(3);
                return new ItemsAdderIcon(id);
            }
            Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(str);
            if (pair != null) {
                return new VanillaIcon(pair);
            }
        }
        return DefaultIcon.INSTANCE;
    }
}
