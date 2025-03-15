package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskSubmitItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

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
        TaskManager manager = TaskManager.inst();
        TaskIcon icon = taskIcons.get(id);
        if (icon != null) {
            // 寻找图标对应任务
            Pair<LoadedTask, TaskCache> pair = gui.getTask(icon.type, icon.index);
            if (pair != null) {
                return icon.generateIcon(player, formatSubTasks, pair.getKey(), pair.getValue());
            } else {
                // 找不到相关任务时
                if (icon.redirectIcon != null) {
                    otherIcons.get(icon.redirectIcon);
                } else {
                    return new ItemStack(Material.AIR);
                }
            }
        }
        LoadedIcon otherIcon = otherIcons.get(id);
        if (otherIcon != null) {
            IModifier<List<String>> loreModifier = old -> {
                List<String> lore = new ArrayList<>();
                EnumTaskType refreshType = null;
                int refreshLimit = 0, refreshCount = 0, refreshAvailable = 0;
                Boolean refresh = null;
                for (String s : old) {
                    if (refreshType != null) break;
                    if (s.startsWith("refresh_operation:")) {
                        EnumTaskType type = Util.valueOr(EnumTaskType.class, s.substring(18).trim(), null);
                        if (type != null) switch (type) {
                            case DAILY:
                                refreshLimit = manager.getDailyCount(player);
                                refreshCount = gui.playerCache.getRefreshCountDaily();
                                break;
                            case WEEKLY:
                                refreshLimit = manager.getWeeklyCount(player);
                                refreshCount = gui.playerCache.getRefreshCountWeekly();
                                break;
                            case MONTHLY:
                                refreshLimit = manager.getMonthlyCount(player);
                                refreshCount = gui.playerCache.getRefreshCountMonthly();
                                break;
                            default:
                                continue;
                        }
                        refreshType = type;
                        refreshAvailable = Math.max(0, refreshLimit - refreshCount);
                        refresh = gui.playerCache.canRefresh(type, refreshLimit);
                    }
                }
                List<Pair<String, Object>> replacements = new ArrayList<>();
                replacements.add(Pair.of("%count%", refreshCount));
                replacements.add(Pair.of("%max%", refreshLimit));
                replacements.add(Pair.of("%times%", refreshAvailable));
                for (String s : old) {
                    if (s.startsWith("refresh_operation:") && refreshType != null) {
                        List<String> list;
                        if (refresh == null) {
                            list = opRefreshTaskDone;
                        } else if (refresh) {
                            list = opRefreshAvailable;
                        } else {
                            list = opRefreshMaxTimes;
                        }
                        for (String line : list) {
                            lore.add(Pair.replace(line, replacements));
                        }
                        continue;
                    }
                    lore.add(Pair.replace(s, replacements));
                }
                replacements.clear();
                return lore;
            };
            return otherIcon.generateIcon(player, null, loreModifier);
        }
        return null;
    }

    public boolean click(
            Menus.Impl gui, Player player,
            TaskIcon icon, ClickType click, int invSlot
    ) {
        Pair<LoadedTask, TaskCache> pair = gui.getTask(icon.type, icon.index);
        if (pair != null) {
            LoadedTask task = pair.getKey();
            TaskCache cache = pair.getValue();
            // 主要图标点击
            if (cache.hasDone()) return false;
            boolean taskDone = true;
            boolean submitItemFlag = false;
            for (int i = 0; i < task.subTasks.size(); i++) {
                ITask subTask = task.subTasks.get(i);
                String taskType = subTask.type();
                Integer value = cache.get(i, taskType);
                if (value == null) {
                    cache.put(i, taskType, 0);
                    value = 0;
                }
                if (subTask instanceof TaskSubmitItem) {
                    TaskSubmitItem submitItem = (TaskSubmitItem) subTask;
                    // 提交任务物品
                    PlayerInventory inv = player.getInventory();
                    int target = submitItem.getTargetValue();
                    if (value < target) {
                        for (int slot = 0; slot < inv.getSize(); slot++) {
                            ItemStack item = inv.getItem(slot);
                            if (submitItem.isItemMatch(item)) {
                                int amount = item.getAmount();
                                if (value + amount > target) {
                                    item.setAmount(amount - (target - value));
                                    value = target;
                                } else {
                                    value += amount;
                                    item.setAmount(0);
                                    item.setType(Material.AIR);
                                    item = null;
                                }
                                inv.setItem(slot, item);
                                cache.put(i, taskType, value);
                                submitItemFlag = true;
                                if (value == target) break;
                            }
                        }
                    }
                }
                if (value < subTask.getTargetValue()) {
                    taskDone = false;
                }
            }
            if (taskDone) {
                cache.put(task.id, 1);
                task.giveRewards(player);
                gui.getPlugin().getDatabase().submitCache(player);
            } else {
                if (submitItemFlag) {
                    t(player, "&a已提交物品到任务");
                } else {
                    t(player, "&e任务未完成");
                }
                // TODO: 提示任务未完成，或者已提交任务物品
                return true;
            }
        } else {
            LoadedIcon otherIcon = otherIcons.get(icon.redirectIcon);
            if (otherIcon != null) {
                otherIcon.click(player, click);
            }
        }
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
