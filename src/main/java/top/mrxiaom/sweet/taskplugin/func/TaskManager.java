package top.mrxiaom.sweet.taskplugin.func;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
    public TaskManager(SweetTask plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
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

    @Nullable
    public LoadedTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public Collection<LoadedTask> getTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    public static TaskManager inst() {
        return instanceOf(TaskManager.class);
    }
}
