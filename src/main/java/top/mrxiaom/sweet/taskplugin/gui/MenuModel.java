package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuModel implements IModel {
    private final String id, title;
    private final char[] inventory;
    private final String permission;
    private final List<String> opRefreshAvailable, opRefreshTaskDone, opRefreshMaxTimes, formatSubTasks;
    private final Map<Character, TaskIcon> taskIcons;
    private final Map<Character, LoadedIcon> otherIcons;

    public MenuModel(String id, String title, char[] inventory, String permission, List<String> opRefreshAvailable, List<String> opRefreshTaskDone, List<String> opRefreshMaxTimes, List<String> formatSubTasks, Map<Character, TaskIcon> taskIcons, Map<Character, LoadedIcon> otherIcons) {
        this.id = id;
        this.title = title;
        this.inventory = inventory;
        this.permission = permission;
        this.opRefreshAvailable = opRefreshAvailable;
        this.opRefreshTaskDone = opRefreshTaskDone;
        this.opRefreshMaxTimes = opRefreshMaxTimes;
        this.formatSubTasks = formatSubTasks;
        this.taskIcons = taskIcons;
        this.otherIcons = otherIcons;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public char[] inventory() {
        return inventory;
    }

    public boolean hasPermission(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    @Override
    public Map<Character, LoadedIcon> otherIcons() {
        return otherIcons;
    }

    @Nullable
    public TaskIcon getTaskIcon(Character id) {
        return taskIcons.get(id);
    }

    @Override
    public ItemStack applyMainIcon(
            IGui instance, Player player,
            char id, int index, int appearTimes
    ) {
        Menus.Impl gui = (Menus.Impl) instance;
        TaskIcon icon = taskIcons.get(id);
        if (icon != null) {
            // TODO: 渲染主要任务图标
        }
        LoadedIcon otherIcon = otherIcons.get(id);
        if (otherIcon != null) {
            IModifier<List<String>> loreModifier = old -> {
                List<String> lore = new ArrayList<>();
                for (String s : old) {
                    if (s.equals("refresh_operation")) {
                        // TODO: 替换 refresh_operation
                        continue;
                    }
                    lore.add(s);
                }
                return lore;
            };
            return otherIcon.generateIcon(player, null, loreModifier);
        }
        return null;
    }

    public boolean click(
            Menus.Impl gui, Player player,
            TaskIcon icon, ClickType click, int slot
    ) {
        // TODO: 主要图标点击
        return false;
    }

    public static MenuModel load(Menus parent, ConfigurationSection config, String id) {
        ConfigurationSection section;
        String title = config.getString("title", "").replace("%id%", id);
        String permission = config.getString("permission", "").replace("%id%", id);
        char[] inventory = String.join("", config.getStringList("inventory")).toCharArray();
        List<String> opRefreshAvailable = config.getStringList("operations.refresh.available"),
                opRefreshTaskDone = config.getStringList("operations.refresh.task-done"),
                opRefreshMaxTimes = config.getStringList("operations.refresh.max-times"),
                formatSubTasks = config.getStringList("format.subtasks");

        Map<Character, TaskIcon> taskIcons = new HashMap<>();
        section = config.getConfigurationSection("task-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            if (key.length() > 1) {
                parent.warn("[menus/" + id + "] 任务图标 " + key + " 的名称过长，请改为一个字符");
                continue;
            }
            char iconId = key.charAt(0);
            TaskIcon loaded = TaskIcon.load(parent, id, section, key);
            if (loaded != null) {
                taskIcons.put(iconId, loaded);
            }
        }

        Map<Character, LoadedIcon> otherIcons = new HashMap<>();
        section = config.getConfigurationSection("other-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            if (key.length() > 1) {
                parent.warn("[menus/" + id + "] 额外图标 " + key + " 的名称过长，请改为一个字符");
                continue;
            }
            char iconId = key.charAt(0);
            LoadedIcon loaded = LoadedIcon.load(section, key);
            otherIcons.put(iconId, loaded);
        }
        return new MenuModel(id, title, inventory, permission.isEmpty() ? null : permission,
                opRefreshAvailable, opRefreshTaskDone, opRefreshMaxTimes, formatSubTasks,
                taskIcons, otherIcons);
    }
}
