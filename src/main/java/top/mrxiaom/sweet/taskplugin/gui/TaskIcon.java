package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    private String parseTime(LocalDateTime time) {
        // TODO: 时间格式移到配置文件
        return time.toLocalDate() + " " + time.toLocalTime();
    }

    private String parseTime(long seconds) {
        // TODO: 时间格式移到配置文件
        String timeDays = "天", timeDay = "天",
            timeHours = "时", timeHour = "时",
            timeMinutes = "分", timeMinute = "分",
            timeSeconds = "秒", timeSecond = "秒";
        long day = seconds / 86400L;
        long hour = (seconds / 3600L) % 24L;
        long minute = (seconds / 60L) % 60L;
        long second = seconds % 60L;
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(day > 1 ? timeDays : timeDay);
        }
        if (day > 0 || hour > 0) {
            sb.append(hour).append(hour > 1 ? timeHours : timeHour);
        }
        if (day > 0 || hour > 0 || minute > 0) {
            sb.append(minute).append(minute > 1 ? timeMinutes : timeMinute);
        }
        sb.append(second).append(second > 1 ? timeSeconds : timeSecond);
        return sb.toString();
    }

    public ItemStack generateIcon(Player player, List<String> formatSubTasks, List<String> operation, LoadedTask task, TaskCache cache) {
        List<Pair<String, Object>> pairs = new ArrayList<>();
        long seconds = Math.max(0, cache.expireTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        pairs.add(Pair.of("%name%", task.name));
        pairs.add(Pair.of("%outdate%", parseTime(cache.expireTime)));
        pairs.add(Pair.of("%remaining_time%", parseTime(seconds)));
        IModifier<String> displayModifier = oldName -> Pair.replace(oldName, pairs);
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
                            int value = subTask.getValue(player, task, cache, i);
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
            return Pair.replace(lore, pairs);
        };
        ItemStack baseItem = task.getIcon(cache.hasDone());
        return icon.generateIcon(baseItem, player, displayModifier, loreModifier);
    }

    @Nullable
    public static TaskIcon load(Menus parent, String parentId, ConfigurationSection section, String key) {
        List<String> typeString = CollectionUtils.split(section.getString(key + ".type", ""), '/');
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
