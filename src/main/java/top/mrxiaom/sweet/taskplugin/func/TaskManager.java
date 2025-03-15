package top.mrxiaom.sweet.taskplugin.func;

import org.bukkit.Bukkit;
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
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
    private int taskDailyCount, taskWeeklyCount, taskMonthlyCount;
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
            return false;
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        ConfigurationSection section;

        if (!parseResetTime(config.getString("reset-time", "4:00:00"))) {
            warn("reset-time 设定错误，已使用默认值 4:00:00");
            resetTime = LocalTime.of(4, 0, 0);
        }

        taskDailyCounts.clear();
        taskWeeklyCounts.clear();
        taskMonthlyCounts.clear();
        taskDailyCount = taskWeeklyCount = taskMonthlyCount = 0;

        section = config.getConfigurationSection("counts.daily");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) {
                taskDailyCount = count;
            }
            String perm = "sweettask.count.daily." + key;
            taskDailyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("counts.weekly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) {
                taskWeeklyCount = count;
            }
            String perm = "sweettask.count.weekly." + key;
            taskWeeklyCounts.add(Pair.of(perm, count));
        }
        section = config.getConfigurationSection("counts.monthly");
        if (section != null) for (String key : section.getKeys(false)) {
            int count = section.getInt(key);
            if (count < 0) continue;
            if (key.equals("default")) {
                taskMonthlyCount = count;
            }
            String perm = "sweettask.count.monthly." + key;
            taskMonthlyCounts.add(Pair.of(perm, count));
        }

        taskDailyCounts.sort(permComparator.reversed());
        taskWeeklyCounts.sort(permComparator.reversed());
        taskMonthlyCounts.sort(permComparator.reversed());

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
        info("加载了 " + tasks.size() + " 个任务");
    }

    public void showActionTips(Player player, TaskWrapper wrapper, int data) {
        ITask subTask = wrapper.subTask;
        String tips = subTask.actionTips();
        if (tips.isEmpty() || player.hasPermission("sweet.task.settings.hide-actionbar")) return;
        int target = subTask.getTargetValue();
        List<Pair<String, Object>> replacements = new ArrayList<>();
        replacements.add(Pair.of("%current%", Math.min(data, target)));
        replacements.add(Pair.of("%max%", target));
        String actionMessage = Pair.replace(tips, replacements);
        replacements.clear();
        AdventureUtil.sendActionBar(player, actionMessage);
    }

    public void checkTasksAsync(PlayerCache playerCaches) {
        checkTasksAsync(playerCaches, null);
    }

    public void checkTasksAsync(PlayerCache playerCaches, Runnable done) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (checkTasks(playerCaches)) {
                plugin.getDatabase().cleanExpiredTasks(playerCaches.player);
                plugin.getDatabase().submitCache(playerCaches);
            }
            if (done != null) {
                Bukkit.getScheduler().runTask(plugin, done);
            }
        });
    }

    public boolean checkTasks(PlayerCache playerCaches) {
        boolean modified = playerCaches.removeOutdatedTasks();
        Player player = playerCaches.player;
        int needDaily = getDailyCount(player);
        int needWeekly = getWeeklyCount(player);
        int needMonthly = getMonthlyCount(player);
        Set<String> tasksDaily = new HashSet<>();
        Set<String> tasksWeekly = new HashSet<>();
        Set<String> tasksMonthly = new HashSet<>();
        for (TaskCache sub : playerCaches.tasks.values()) {
            LoadedTask task = getTask(sub.taskId);
            if (task == null) continue;
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
        if (needDaily > 0) {
            List<String> available = new ArrayList<>(tasksByDaily.keySet());
            available.removeAll(tasksDaily);
            while (--needDaily >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                }
            }
        }
        if (needWeekly > 0) {
            List<String> available = new ArrayList<>(tasksByWeekly.keySet());
            available.removeAll(tasksWeekly);
            while (--needWeekly >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                }
            }
        }
        if (needMonthly > 0) {
            List<String> available = new ArrayList<>(tasksByMonthly.keySet());
            available.removeAll(tasksMonthly);
            while (--needMonthly >= 0) {
                if (available.isEmpty()) break;
                int index = new Random().nextInt(available.size());
                String id = available.remove(index);
                LoadedTask task = getTask(id);
                if (task != null) {
                    modified = true;
                    LocalDateTime expireTime = nextOutdate(task.type);
                    playerCaches.addTask(task, expireTime);
                }
            }
        }
        return modified;
    }

    public int getDailyCount(Permissible player) {
        return getCount(taskDailyCounts, player, taskDailyCount);
    }

    public int getWeeklyCount(Permissible player) {
        return getCount(taskWeeklyCounts, player, taskWeeklyCount);
    }

    public int getMonthlyCount(Permissible player) {
        return getCount(taskMonthlyCounts, player, taskMonthlyCount);
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
        boolean addOneDay = now.toLocalTime().isBefore(resetTime);
        switch (type) {
            case DAILY: {
                LocalDateTime time = now.toLocalDate()
                        .plusDays(1)
                        .atTime(resetTime);
                return addOneDay ? time.plusDays(1) : time;
            }
            case WEEKLY:
                LocalDate date;
                if (!now.getDayOfWeek().equals(DayOfWeek.MONDAY)) { // 重置回周一
                    date = now.toLocalDate()
                            .minusDays(now.getDayOfWeek().getValue() - 1)
                            .plusWeeks(1);
                } else {
                    date = now.toLocalDate()
                            .plusWeeks(1);
                }
                return date.atTime(resetTime);
            case MONTHLY:
                if (now.getDayOfMonth() == 1 && addOneDay) {
                    return now.toLocalDate().atTime(resetTime);
                }
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
