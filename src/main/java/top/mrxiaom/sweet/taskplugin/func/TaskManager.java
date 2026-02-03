package top.mrxiaom.sweet.taskplugin.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.func.entry.TaskTypeInstance;
import top.mrxiaom.sweet.taskplugin.listeners.wrapper.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static top.mrxiaom.sweet.taskplugin.SweetTask.DEBUG;

@AutoRegister
public class TaskManager extends AbstractModule {
    private final Map<String, LoadedTask> tasks = new HashMap<>();
    private final Comparator<Pair<String, Integer>> permComparator = Comparator.comparingInt(Pair::getValue);
    private final Map<EnumTaskType, TaskTypeInstance> taskTypes = new HashMap<>();
    private LocalTime resetTime;
    public TaskManager(SweetTask plugin) {
        super(plugin);
        for (EnumTaskType taskType : EnumTaskType.values()) {
            taskTypes.put(taskType, new TaskTypeInstance(taskType));
        }
    }

    @NotNull
    public TaskTypeInstance typeDaily() {
        return type(EnumTaskType.DAILY);
    }

    @NotNull
    public TaskTypeInstance typeWeekly() {
        return type(EnumTaskType.WEEKLY);
    }

    @NotNull
    public TaskTypeInstance typeMonthly() {
        return type(EnumTaskType.MONTHLY);
    }

    public TaskTypeInstance type(EnumTaskType type) {
        return taskTypes.get(type);
    }

    private boolean parseResetTime(String str) {
        String[] split = str.split(":");
        if (split.length == 3) {
            Integer hour = Util.parseInt(split[0]).orElse(null);
            Integer minute = Util.parseInt(split[1]).orElse(null);
            Integer second = Util.parseInt(split[2]).orElse(null);
            if (hour == null || minute == null || second == null) return false;
            resetTime = LocalTime.of(hour, minute, second);
            return true;
        } else if (split.length == 2) {
            Integer hour = Util.parseInt(split[0]).orElse(null);
            Integer minute = Util.parseInt(split[1]).orElse(null);
            if (hour == null || minute == null) return false;
            resetTime = LocalTime.of(hour, minute);
            return true;
        } else {
            resetTime = LocalTime.of(4, 0, 0);
            return false;
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (!parseResetTime(config.getString("reset-time", "4:00:00"))) {
            warn("[config.yml] reset-time 设定错误，已使用默认值 4:00:00");
        }

        reloadMaxTasksCounts(config);
        reloadMaxRefreshCounts(config);
        reloadTasks(config);

        StringBuilder sb = new StringBuilder();
        sb.append("加载了 ").append(tasks.size()).append(" 个任务");
        for (TaskTypeInstance type : taskTypes.values()) {
            sb.append("，");
            sb.append(type.getDisplayInLog());
            sb.append("[").append(type.getTasks().size()).append("]");
        }
        info(sb.toString());
    }

    private void reloadMaxTasksCounts(MemoryConfiguration config) {
        for (TaskTypeInstance type : taskTypes.values()) {
            List<Pair<String, Integer>> taskCounts = type.getMaxTaskCounts();
            taskCounts.clear();
            int taskCountDefault = 0;
            ConfigurationSection section = config.getConfigurationSection("counts." + type.key());
            if (section != null) for (String key : section.getKeys(false)) {
                int count = section.getInt(key);
                if (count < 0) continue;
                if (key.equals("default")) taskCountDefault = count;
                String perm = "sweettask.count." + type.key() + "." + key;
                taskCounts.add(Pair.of(perm, count));
            }
            type.setMaxTaskCountDefault(taskCountDefault);
            taskCounts.sort(permComparator.reversed());
        }
    }

    private void reloadMaxRefreshCounts(MemoryConfiguration config) {
        for (TaskTypeInstance type : taskTypes.values()) {
            List<Pair<String, Integer>> refreshCounts = type.getMaxRefreshCounts();
            refreshCounts.clear();
            int refreshCountDefault = 0;
            ConfigurationSection section = config.getConfigurationSection("refresh-counts." + type.key());
            if (section != null) for (String key : section.getKeys(false)) {
                int count = section.getInt(key);
                if (count < 0) continue;
                if (key.equals("default")) refreshCountDefault = count;
                String perm = "sweettask.refresh-count." + type.key() + "." + key;
                refreshCounts.add(Pair.of(perm, count));
            }
            type.setMaxRefreshCountDefault(refreshCountDefault);
            refreshCounts.sort(permComparator.reversed());
        }
    }
    private void reloadTasks(MemoryConfiguration config) {
        tasks.clear();
        for (TaskTypeInstance type : taskTypes.values()) {
            type.clearTasks();
        }
        for (String path : config.getStringList("tasks-folder")) {
            File folder = plugin.resolve(path);
            if (!folder.exists()) {
                Util.mkdirs(folder);
                if (path.equals("./tasks")) {
                    plugin.saveResource("tasks/example.yml", new File(folder, "example.yml"));
                }
            }
            Util.reloadFolder(folder, false, (id, file) -> {
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                LoadedTask loaded = LoadedTask.load(this, cfg, id);
                if (loaded != null) {
                    tasks.put(loaded.id, loaded);
                    TaskTypeInstance type = taskTypes.get(loaded.type);
                    if (type != null) {
                        type.addTask(loaded);
                    }
                }
            });
        }
    }

    public void showActionTips(Player player, TaskWrapper wrapper, int data) {
        ITask subTask = wrapper.subTask;
        String tips = subTask.actionTips();
        if (tips.isEmpty() || player.hasPermission("sweet.task.settings.hide-actionbar")) return;
        List<Pair<String, Object>> replacements = subTask.actionReplacements(data);
        String actionMessage = Pair.replace(tips, replacements);
        replacements.clear();
        AdventureUtil.sendActionBar(player, actionMessage);
    }

    public void checkTasksAsync(PlayerCache playerCaches) {
        checkTasksAsync(playerCaches, null);
    }

    public void checkTasksAsync(PlayerCache playerCaches, Runnable done) {
        plugin.getScheduler().runTaskAsync(() -> {
            if (checkTasks(playerCaches)) { // 返回值是“任务列表是否有被修改”
                // 如果任务列表被改了（多了任务或者少了任务），就清空数据库上的任务列表重新上传
                plugin.getDatabase().submitCache(playerCaches, true);
            }
            if (done != null) {
                plugin.getScheduler().runTask(done);
            }
        });
    }

    private List<String> getWeightedKeys(Map<String, LoadedTask> tasks, Permissible p) {
        List<String> list = new ArrayList<>();
        for (LoadedTask task : tasks.values()) {
            if (task.hasPermission(p)) {
                for (int i = 0; i < task.weight; i++) {
                    list.add(task.id);
                }
            }
        }
        return list;
    }

    private static class CheckTaskType {
        private final TaskTypeInstance type;
        private final Set<String> taskIds = new HashSet<>();
        private int needed;
        private CheckTaskType(TaskTypeInstance type, Permissible player) {
            this.type = type;
            this.needed = type.getMaxTasksCount(player);
        }
        private static Map<EnumTaskType, CheckTaskType> inst(Map<EnumTaskType, TaskTypeInstance> taskTypes, Permissible player) {
            Map<EnumTaskType, CheckTaskType> checkTypes = new HashMap<>();
            for (TaskTypeInstance type : taskTypes.values()) {
                checkTypes.put(type.type(), new CheckTaskType(type, player));
            }
            return checkTypes;
        }
    }

    public boolean checkTasks(PlayerCache playerCaches) {
        boolean modified = playerCaches.removeOutdatedTasks(); // 先清理一下过期的任务
        Player player = playerCaches.player;
        Map<EnumTaskType, CheckTaskType> checkTypes = CheckTaskType.inst(taskTypes, player);
        for (TaskCache sub : playerCaches.tasks.values()) { // 遍历所有已缓存任务
            LoadedTask task = getTask(sub.taskId);
            if (task == null) continue;
            // 根据已缓存任务，计算各类型任务需求数量
            CheckTaskType check = checkTypes.get(task.type);
            if (check != null) {
                check.taskIds.add(task.id);
                if (check.needed > 0) check.needed--;
            }
        }
        if (DEBUG) {
            boolean log = false;
            StringBuilder sb = new StringBuilder();
            sb.append("玩家").append(player.getName()).append("需要 ");
            for (CheckTaskType check : checkTypes.values()) {
                if (check.needed > 0) {
                    if (!log) log = true;
                    else sb.append("、");

                    sb.append(check.needed);
                    sb.append(check.type.getDisplayInLog());
                }
            }
            if (log) {
                info(sb.append("任务").toString());
            }
        }
        for (CheckTaskType check : checkTypes.values()) {
            // 在玩家的任务数量不足的时候，为玩家添加任务
            if (check.needed > 0) {
                List<String> available = getWeightedKeys(check.type.getTasks(), player);
                available.removeIf(check.taskIds::contains);
                if (DEBUG) {
                    HashSet<String> sets = new HashSet<>(available);
                    info("可用的" + check.type.getDisplayInLog() + "任务数量: " + sets.size());
                    sets.clear();
                }
                if (doAddTasks(check.needed, available, playerCaches)) modified = true;
                // 对临时新建的列表进行清理
                available.clear();
                check.taskIds.clear();
            }
        }
        return modified;
    }

    private boolean doAddTasks(int needCount, List<String> weightedKeys, PlayerCache playerCache) {
        boolean modified = false;
        int needed = needCount;
        while (--needed >= 0) {
            if (weightedKeys.isEmpty()) break;
            int index = new Random().nextInt(weightedKeys.size());

            String id = weightedKeys.get(index); // 按权重随机获取任务键
            weightedKeys.removeIf(id::equals);  // 获取之后，移除按权重列表的所有键
            LoadedTask task = getTask(id); // 获取已加载任务配置

            if (task != null) {
                modified = true;
                LocalDateTime expireTime = nextOutdate(task.type);
                if (DEBUG) info("添加任务 " + task.id + ": " + task.name + " (" + expireTime + "过期)");
                playerCache.addTask(task, expireTime);
            } else {
                if (DEBUG) warn("任务 " + id + " 不存在");
            }
        }
        return modified;
    }

    public int getMaxRefreshCount(Permissible player, EnumTaskType type) {
        TaskTypeInstance instance = taskTypes.get(type);
        if (instance != null) {
            return instance.getMaxRefreshCount(player);
        }
        return 0;
    }

    public LocalDateTime nextOutdate(EnumTaskType type) {
        return nextOutdate(LocalDateTime.now(), type, resetTime);
    }

    public static LocalDateTime nextOutdate(LocalDateTime now, EnumTaskType type, LocalTime resetTime) {
        LocalTime currentTime = now.toLocalTime();
        switch (type) {
            case DAILY: {
                LocalDateTime time = now.toLocalDate()
                        .atTime(resetTime);
                // 如果 当前时间 已经过了 过期时间
                if (currentTime.isAfter(resetTime)) {
                    // +1天
                    return time.plusDays(1);
                } else {
                    return time;
                }
            }
            case WEEKLY:
                LocalDate date;
                // 如果今天不是周一，重置回本周一，再加一周
                if (!now.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                    date = now.toLocalDate()
                            .minusDays(now.getDayOfWeek().getValue() - 1)
                            .plusWeeks(1);
                    // 返回下一周的过期时间
                    return date.atTime(resetTime);
                } else {
                    // 如果今天是周一，且 当前时间 已经过了 过期时间
                    if (currentTime.isAfter(resetTime)) {
                        // 返回下一周的 过期时间
                        date = now.toLocalDate()
                                .plusWeeks(1);
                        return date.atTime(resetTime);
                    } else {
                        // 如果今天是周一，没到 过期时间，返回今天的 过期时间
                        return now.toLocalDate().atTime(resetTime);
                    }
                }
            case MONTHLY:
                // 如果今天是当月1号
                if (now.getDayOfMonth() == 1) {
                    // 如果当前时间 没有到 过期时间
                    if (!currentTime.isAfter(resetTime)) {
                        // 返回今天的过期时间
                        return now.toLocalDate().atTime(resetTime);
                    }
                }
                // 否则返回下个月1号的 过期时间
                return now.toLocalDate()
                        .plusMonths(1)
                        .withDayOfMonth(1)
                        .atTime(resetTime);
            default:
                throw new IllegalArgumentException(type.name() + " can't calculate next outdate time.");
        }
    }

    @Nullable
    public LoadedTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public Collection<LoadedTask> getTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    public Set<String> getTasksId() {
        return tasks.keySet();
    }

    @Deprecated
    public int getDailyMaxTasksCount(Permissible player) {
        return typeDaily().getMaxTasksCount(player);
    }

    @Deprecated
    public int getWeeklyMaxTasksCount(Permissible player) {
        return typeWeekly().getMaxTasksCount(player);
    }

    @Deprecated
    public int getMonthlyMaxTasksCount(Permissible player) {
        return typeMonthly().getMaxTasksCount(player);
    }

    @Deprecated
    public int getDailyMaxRefreshCount(Permissible player) {
        return typeDaily().getMaxRefreshCount(player);
    }

    @Deprecated
    public int getWeeklyMaxRefreshCount(Permissible player) {
        return typeWeekly().getMaxRefreshCount(player);
    }

    @Deprecated
    public int getMonthlyMaxRefreshCount(Permissible player) {
        return typeMonthly().getMaxRefreshCount(player);
    }

    public static TaskManager inst() {
        return instanceOf(TaskManager.class);
    }
}
