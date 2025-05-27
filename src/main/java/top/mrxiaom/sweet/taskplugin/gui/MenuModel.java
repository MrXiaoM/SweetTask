package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
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

public class MenuModel extends AbstractModel<TaskIcon, MenuModel.Data> {
    private final List<String> opRefreshAvailable, opRefreshTaskDone, opRefreshMaxTimes,
            opTaskAvailable, opTaskDone, formatSubTasks;

    public MenuModel(String id, String title, char[] inventory, String permission,
                     List<String> opRefreshAvailable, List<String> opRefreshTaskDone, List<String> opRefreshMaxTimes,
                     List<String> opTaskAvailable, List<String> opTaskDone, List<String> formatSubTasks,
                     Map<Character, TaskIcon> taskIcons, Map<Character, LoadedIcon> otherIcons
    ) {
        super(id, title, inventory, permission, taskIcons, otherIcons);
        this.opRefreshAvailable = opRefreshAvailable;
        this.opRefreshTaskDone = opRefreshTaskDone;
        this.opRefreshMaxTimes = opRefreshMaxTimes;
        this.opTaskAvailable = opTaskAvailable;
        this.opTaskDone = opTaskDone;
        this.formatSubTasks = formatSubTasks;
    }

    @Override
    public ItemStack applyMainIcon(
            Menus.Impl<TaskIcon, Data> gui, Player player,
            char id, int index, int appearTimes
    ) {
        TaskManager manager = TaskManager.inst();
        TaskIcon icon = mainIcon(id);
        if (icon != null) {
            // 寻找图标对应任务
            Pair<LoadedTask, TaskCache> pair = gui.data.getTask(icon.type, icon.index);
            if (pair != null) {
                List<String> operation = pair.getValue().hasDone()
                        ? opTaskDone
                        : opTaskAvailable;
                return icon.generateIcon(player, formatSubTasks, operation, pair.getKey(), pair.getValue());
            } else {
                // 找不到相关任务时
                if (icon.redirectIcon != null) {
                    otherIcon(icon.redirectIcon);
                } else {
                    return new ItemStack(Material.AIR);
                }
            }
        }
        LoadedIcon otherIcon = otherIcon(id);
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
                                refreshLimit = manager.getDailyMaxRefreshCount(player);
                                refreshCount = gui.playerCache.getRefreshCountDaily();
                                break;
                            case WEEKLY:
                                refreshLimit = manager.getWeeklyMaxRefreshCount(player);
                                refreshCount = gui.playerCache.getRefreshCountWeekly();
                                break;
                            case MONTHLY:
                                refreshLimit = manager.getMonthlyMaxRefreshCount(player);
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

    @Override
    public Data createData(Player player, PlayerCache playerCache) {
        return new Data(playerCache);
    }

    public static class Data implements IMenuData {
        public List<Pair<LoadedTask, TaskCache>> tasksDaily, tasksWeekly, tasksMonthly;
        private final PlayerCache playerCache;
        public Data(PlayerCache playerCache) {
            this.playerCache = playerCache;
        }

        @Override
        public void load() {
            if (this.tasksDaily != null) this.tasksDaily.clear();
            if (this.tasksWeekly != null) this.tasksWeekly.clear();
            if (this.tasksMonthly != null) this.tasksMonthly.clear();
            this.tasksDaily = playerCache.getTasksByType(EnumTaskType.DAILY);
            this.tasksWeekly = playerCache.getTasksByType(EnumTaskType.WEEKLY);
            this.tasksMonthly = playerCache.getTasksByType(EnumTaskType.MONTHLY);
        }

        @Nullable
        public Pair<LoadedTask, TaskCache> getTask(EnumTaskType type, int index) {
            if (index < 0) return null;
            List<Pair<LoadedTask, TaskCache>> list;
            switch (type) {
                case DAILY:
                    list = tasksDaily;
                    break;
                case WEEKLY:
                    list = tasksWeekly;
                    break;
                case MONTHLY:
                    list = tasksMonthly;
                    break;
                default:
                    throw new IllegalArgumentException(type.name() + " is not supported.");
            }
            if (index >= list.size()) return null;
            return list.get(index);
        }
    }

    @Override
    public boolean click(
            Menus.Impl<TaskIcon, Data> gui, Player player,
            TaskIcon icon, ClickType click, int invSlot
    ) {
        Pair<LoadedTask, TaskCache> pair = gui.data.getTask(icon.type, icon.index);
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
                int value = cache.get(i, taskType);
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
                if (SweetTask.DEBUG) {
                    SweetTask.getInstance().info("玩家 " + player.getName() + " 完成了任务 " + task.name + " (" + task.id + ")");
                }
                cache.put(task.id, 1);
                task.giveRewards(player);
                gui.getPlugin().getDatabase().submitCache(player);
            } else {
                if (submitItemFlag) {
                    t(player, "&a已提交物品到任务");
                } else {
                    t(player, "&e任务未完成");
                }
            }
            return true;
        } else {
            LoadedIcon otherIcon = otherIcon(icon.redirectIcon);
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
                opTaskAvailable = config.getStringList("operations.task.available"),
                opTaskDone = config.getStringList("operations.task.done"),
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
                opRefreshAvailable, opRefreshTaskDone, opRefreshMaxTimes,
                opTaskAvailable, opTaskDone, formatSubTasks,
                taskIcons, otherIcons);
    }
}
