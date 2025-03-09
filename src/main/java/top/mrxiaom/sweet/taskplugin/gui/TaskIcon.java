package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

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

    public ItemStack generateIcon(Player player, List<String> formatSubTasks, LoadedTask task, TaskCache cache) {
        // TODO: 主要图标
        return null;
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
