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
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.listeners.TaskWrapper;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

import java.io.File;
import java.util.*;

@AutoRegister
public class TaskManager extends AbstractModule {
    private final Map<String, LoadedTask> tasks = new HashMap<>();
    private final Comparator<Pair<String, Integer>> permComparator = Comparator.comparingInt(Pair::getValue);
    private final List<Pair<String, Integer>> taskDailyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> taskWeeklyCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> taskMonthlyCounts = new ArrayList<>();
    private int taskDailyCount, taskWeeklyCount, taskMonthlyCount;
    public TaskManager(SweetTask plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        ConfigurationSection section;
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
