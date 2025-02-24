package top.mrxiaom.sweet.taskplugin.func;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
}
