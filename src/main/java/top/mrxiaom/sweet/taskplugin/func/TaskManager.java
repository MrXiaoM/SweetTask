package top.mrxiaom.sweet.taskplugin.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
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
    private final Map<String, LoadedTask> tasksByDaily = new HashMap<>();
    private final Map<String, LoadedTask> tasksByWeekly = new HashMap<>();
    private final Map<String, LoadedTask> tasksByMonthly = new HashMap<>();
    private final Comparator<Pair<String, Integer>> permComparator = Comparator.comparingInt(Pair::getValue);
    private final List<Pair<String, Integer>> taskDailyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> taskWeeklyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> taskMonthlyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> refreshDailyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> refreshWeeklyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> refreshMonthlyCounts = new ArrayList<>();
    private int taskDailyCount, taskWeeklyCount, taskMonthlyCount;
    private int refreshDailyCount, refreshWeeklyCount, refreshMonthlyCount;
    private LocalTime resetTime;
    public TaskManager(SweetTask plugin) {
        super(plugin);
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

        info("加载了 " + tasks.size() + " 个任务，每日[" + tasksByDaily.size() + "]，每周[" + tasksByWeekly.size() + "]，每月[" + tasksByMonthly.size() + "]");
    }

    private void reloadMaxTasksCounts(MemoryConfiguration config) {
        ConfigurationSection section;

        taskDailyCounts.clear();
        taskWeeklyCounts.clear();
        taskMonthlyCounts.clear();
        taskDailyCount = taskWeeklyCount = taskMonthlyCount = 0;

        section = config.getConfigurationSection("counts.daily");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) taskDailyCount = count;
            String perm = "sweettask.count.daily." + key;
            taskDailyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("counts.weekly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) taskWeeklyCount = count;
            String perm = "sweettask.count.weekly." + key;
            taskWeeklyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("counts.monthly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) taskMonthlyCount = count;
            String perm = "sweettask.count.monthly." + key;
            taskMonthlyCounts.add(Pair.of(perm, count));
        }

        taskDailyCounts.sort(permComparator.reversed());
        taskWeeklyCounts.sort(permComparator.reversed());
        taskMonthlyCounts.sort(permComparator.reversed());
    }

    private void reloadMaxRefreshCounts(MemoryConfiguration config) {
        ConfigurationSection section;

        refreshDailyCounts.clear();
        refreshWeeklyCounts.clear();
        refreshMonthlyCounts.clear();
        refreshDailyCount = taskWeeklyCount = taskMonthlyCount = 0;

        section = config.getConfigurationSection("refresh-counts.daily");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) refreshDailyCount = count;
            String perm = "sweettask.refresh-count.daily." + key;
            refreshDailyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("refresh-counts.weekly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) refreshWeeklyCount = count;
            String perm = "sweettask.refresh-count.weekly." + key;
            refreshWeeklyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("refresh-counts.monthly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) refreshMonthlyCount = count;
            String perm = "sweettask.refresh-count.monthly." + key;
            refreshMonthlyCounts.add(Pair.of(perm, count));
        }

        refreshDailyCounts.sort(permComparator.reversed());
        refreshWeeklyCounts.sort(permComparator.reversed());
        refreshMonthlyCounts.sort(permComparator.reversed());
    }
    private void reloadTasks(MemoryConfiguration config) {
        tasks.clear();
        tasksByDaily.clear();
        tasksByWeekly.clear();
        tasksByMonthly.clear();
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
                    switch (loaded.type) {
                        case DAILY:
                            tasksByDaily.put(loaded.id, loaded);
                            break;
                        case WEEKLY:
                            tasksByWeekly.put(loaded.id, loaded);
                            break;
                        case MONTHLY:
                            tasksByMonthly.put(loaded.id, loaded);
                            break;
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

    public boolean checkTasks(PlayerCache playerCaches) {
        boolean modified = playerCaches.removeOutdatedTasks(); // 先清理一下过期的任务
        Player player = playerCaches.player;
        int needDaily = getDailyMaxTasksCount(player);
        int needWeekly = getWeeklyMaxTasksCount(player);
        int needMonthly = getMonthlyMaxTasksCount(player);
        Set<String> tasksDaily = new HashSet<>();
        Set<String> tasksWeekly = new HashSet<>();
        Set<String> tasksMonthly = new HashSet<>();
        for (TaskCache sub : playerCaches.tasks.values()) { // 遍历所有已缓存任务
            LoadedTask task = getTask(sub.taskId);
            if (task == null) continue;
            // 根据已缓存任务，计算各类型任务需求数量
            switch (task.type) {
                case DAILY:
                    tasksDaily.add(task.id);
                    if (needDaily > 0) needDaily--;
                    break;
                case WEEKLY:
                    tasksWeekly.add(task.id);
                    if (needWeekly > 0) needWeekly--;
                    break;
                case MONTHLY:
                    tasksMonthly.add(task.id);
                    if (needMonthly > 0) needMonthly--;
                    break;
            }
        }
        if (DEBUG && (needDaily > 0 || needWeekly > 0 || needMonthly > 0)) {
            info("玩家" + player.getName() + "需要 " + needDaily + "每日、" + needWeekly + "每周、" + needMonthly + "每月任务");
        }
        if (needDaily > 0) {
            List<String> available = new ArrayList<>(tasksByDaily.keySet());
            available.removeAll(tasksDaily);
            if (DEBUG) info("可用的每日任务数量: " + available.size());
            while (--needDaily >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    if (DEBUG) info("添加任务 " + task.id + ": " + task.name);
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                } else {
                    if (DEBUG) warn("任务 " + id + " 不存在");
                }
            }
        }
        if (needWeekly > 0) {
            List<String> available = new ArrayList<>(tasksByWeekly.keySet());
            available.removeAll(tasksWeekly);
            if (DEBUG) info("可用的每周任务数量: " + available.size());
            while (--needWeekly >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    if (DEBUG) info("添加任务 " + task.id + ": " + task.name);
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                } else {
                    if (DEBUG) warn("任务 " + id + " 不存在");
                }
            }
        }
        if (needMonthly > 0) {
            List<String> available = new ArrayList<>(tasksByMonthly.keySet());
            available.removeAll(tasksMonthly);
            if (DEBUG) info("可用的每月任务数量: " + available.size());
            while (--needMonthly >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    if (DEBUG) info("添加任务 " + task.id + ": " + task.name);
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                } else {
                    if (DEBUG) warn("任务 " + id + " 不存在");
                }
            }
        }
        return modified;
    }

    public int getMaxRefreshCount(Permissible player, EnumTaskType type) {
        switch (type) {
            case DAILY:
                return getDailyMaxRefreshCount(player);
            case WEEKLY:
                return getWeeklyMaxRefreshCount(player);
            case MONTHLY:
                return getMonthlyMaxRefreshCount(player);
        }
        return 0;
    }

    public int getDailyMaxTasksCount(Permissible player) {
        return getCount(taskDailyCounts, player, taskDailyCount);
    }

    public int getWeeklyMaxTasksCount(Permissible player) {
        return getCount(taskWeeklyCounts, player, taskWeeklyCount);
    }

    public int getMonthlyMaxTasksCount(Permissible player) {
        return getCount(taskMonthlyCounts, player, taskMonthlyCount);
    }

    public int getDailyMaxRefreshCount(Permissible player) {
        return getCount(refreshDailyCounts, player, refreshDailyCount);
    }

    public int getWeeklyMaxRefreshCount(Permissible player) {
        return getCount(refreshWeeklyCounts, player, refreshWeeklyCount);
    }

    public int getMonthlyMaxRefreshCount(Permissible player) {
        return getCount(refreshMonthlyCounts, player, refreshMonthlyCount);
    }

    private int getCount(List<Pair<String, Integer>> permList, Permissible player, int def) {
        for (Pair<String, Integer> pair : permList) {
            if (player.hasPermission(pair.getKey())) {
                return pair.getValue();
            }
        }
        return def;
    }


    public LocalDateTime nextOutdate(EnumTaskType type) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        switch (type) {
            case DAILY: {
                LocalDateTime time = now.toLocalDate()
                        .plusDays(1)
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
                // 如果今天是当月1号，且没有到 过期时间
                if (now.getDayOfMonth() == 1 && currentTime.isBefore(resetTime)) {
                    // 返回今天的过期时间
                    return now.toLocalDate().atTime(resetTime);
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

    public static TaskManager inst() {
        return instanceOf(TaskManager.class);
    }
}
