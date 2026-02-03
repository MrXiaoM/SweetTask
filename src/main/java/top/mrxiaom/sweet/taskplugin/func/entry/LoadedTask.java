package top.mrxiaom.sweet.taskplugin.func.entry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
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
    private final SweetTask plugin;
    public final String id;
    public final @Nullable String permission;
    public final EnumTaskType type;
    public final int weight;
    public final IconProvider iconNormal;
    public final IconProvider iconDone;
    public final String name;
    public final List<String> description;
    public final List<ITask> subTasks;
    public final List<IAction> rewards;
    public final List<String> rewardsLore;
    public final String overrideDoneTips;

    @Deprecated
    public LoadedTask(String id, EnumTaskType type, int weight,
                      IconProvider iconNormal, IconProvider iconDone,
                      String name, List<String> description, List<ITask> subTasks,
                      List<IAction> rewards, List<String> rewardsLore,
                      String overrideDoneTips
    ) {
        this.plugin = SweetTask.getInstance();
        this.id = id;
        this.permission = null;
        this.type = type;
        this.weight = weight;
        this.iconNormal = iconNormal;
        this.iconDone = iconDone;
        this.name = name;
        this.description = description;
        this.subTasks = subTasks;
        this.rewards = rewards;
        this.rewardsLore = rewardsLore;
        this.overrideDoneTips = overrideDoneTips;
    }

    protected LoadedTask(TaskManager parent, ConfigurationSection config, String id) {
        this.plugin = parent.plugin;
        this.id = id;
        String permission = config.getString("permission", "none");
        if (permission.equalsIgnoreCase("none")) {
            this.permission = null;
        } else {
            this.permission = permission;
        }
        this.weight = config.getInt("rarity", 0);
        if (weight <= 0) {
            throw new IllegalArgumentException("任务稀有度 rarity 的数值有误");
        }
        this.iconNormal = getIcon(parent.plugin, config, "icon.normal");
        this.iconDone = getIcon(parent.plugin, config, "icon.done");
        this.name = config.getString("name", id);
        EnumTaskType type = Util.valueOr(EnumTaskType.class, config.getString("type"), null);
        if (type == null) {
            throw new IllegalArgumentException("任务类型输入有误");
        }
        this.type = type;
        this.description = config.getStringList("description");
        this.subTasks = new ArrayList<>();
        for (String s : config.getStringList("sub-tasks")) {
            ITask task = ITask.load(parent, id, s);
            if (task != null) {
                subTasks.add(task);
            } else {
                parent.warn("[tasks/" + id + "] 无效的子任务: " + s);
            }
        }
        this.rewards = loadActions(config, "rewards");
        this.rewardsLore = config.getStringList("rewards-lore");
        this.overrideDoneTips = config.getString("override-done-tips", null);
    }

    public boolean hasPermission(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    public void giveRewards(Player player) {
        plugin.getScheduler().runTask(() -> ActionProviders.run(plugin, player, rewards));
    }

    @NotNull
    public ItemStack getIcon(boolean done) {
        ItemStack item = (done ? iconDone : iconNormal).create();
        return item != null ? item : new ItemStack(Material.PAPER);
    }

    @Nullable
    public static LoadedTask load(TaskManager parent, ConfigurationSection config, String id) {
        try {
            return new LoadedTask(parent, config, id);
        } catch (Throwable t) {
            parent.warn("[tasks/" + id + "] " + t.getMessage());
            return null;
        }
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
