package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.util.ArrayList;
import java.util.List;

public class TaskIcon {
    /**
     * 任务类型
     */
    public final EnumTaskType type;
    /**
     * 任务索引，比如 0为第一个每日任务
     */
    public final int index;
    public final LoadedIcon icon;
    public final Character redirectIcon;

    public TaskIcon(EnumTaskType type, int index, LoadedIcon icon, Character redirectIcon) {
        this.type = type;
        this.index = index;
        this.icon = icon;
        this.redirectIcon = redirectIcon;
    }

    public ItemStack generateIcon(Player player, List<String> formatSubTasks, List<String> operation, LoadedTask task, TaskCache cache) {
        IModifier<String> displayModifier = oldName -> oldName.replace("%name%", task.name);
        IModifier<List<String>> loreModifier = oldLore -> {
            List<String> lore = new ArrayList<>();
            for (String s : oldLore) {
                switch (s) {
                    case "description":
                        lore.addAll(task.description);
                        continue;
                    case "sub_tasks":
                        for (int i = 0; i < task.subTasks.size(); i++) {
                            ITask subTask = task.subTasks.get(i);
                            String taskType = subTask.type();
                            int value = cache.get(i, taskType);
                            List<Pair<String, Object>> replacements = subTask.actionReplacements(value);
                            String action = Pair.replace(subTask.actionTips(), replacements);
                            replacements.clear();
                            for (String line : formatSubTasks) {
                                lore.add(line.replace("%action%", action));
                            }
                        }
                        continue;
                    case "rewards":
                        lore.addAll(task.rewardsLore);
                        continue;
                    case "operation":
                        lore.addAll(operation);
                        continue;
                }
                lore.add(s);
            }
            return lore;
        };
        ItemStack baseItem = task.getIcon(cache.hasDone());
        return icon.generateIcon(baseItem, player, displayModifier, loreModifier);
    }

    @Nullable
    public static TaskIcon load(Menus parent, String parentId, ConfigurationSection section, String key) {
        List<String> typeString = Util.split(section.getString(key + ".type", ""), '/');
        if (typeString.size() != 2) {
            parent.warn("[menus/" + parentId + "] 任务图标 " + key + " 的 type 格式有误");
            return null;
        }
        EnumTaskType type = Util.valueOr(EnumTaskType.class, typeString.get(0), null);
        Integer count = Util.parseInt(typeString.get(1)).orElse(null);
        if (type == null || count == null || count < 1) {
            parent.warn("[menus/" + parentId + "] 任务图标 " + key + " 的 type 格式有误");
            return null;
        }
        LoadedIcon icon = LoadedIcon.load(section, key);
        String redirectIcon = section.getString(key + ".redirect", "");
        return new TaskIcon(type, count - 1, icon, !redirectIcon.isEmpty() ? redirectIcon.charAt(0) : null);
    }
}
