package top.mrxiaom.sweet.taskplugin;
        
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;
import top.mrxiaom.sweet.taskplugin.mythic.Mythic4;
import top.mrxiaom.sweet.taskplugin.mythic.Mythic5;
import top.mrxiaom.sweet.taskplugin.tasks.*;

public class SweetTask extends BukkitPlugin {
    public static SweetTask getInstance() {
        return (SweetTask) BukkitPlugin.getInstance();
    }

    public SweetTask() {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.sweet.taskplugin.libs")
        );
    }
    TaskProcessDatabase taskProcessDatabase;
    IMythic mythic;

    public IMythic getMythic() {
        return mythic;
    }

    public TaskProcessDatabase getDatabase() {
        return taskProcessDatabase;
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    protected void beforeEnable() {
        Plugin mythicPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mythicPlugin != null) {
            String v = mythicPlugin.getDescription().getVersion();
            if (v.startsWith("5.")) {
                mythic = new Mythic5();
                info("支持 MythicMobs " + v);
            } else if (v.startsWith("4.")) {
                mythic = new Mythic4();
                info("支持 MythicMobs " + v);
            } else {
                warn("不支持的 MythicMobs 版本 " + v);
            }
        }
        registerBuiltInTasks();
        options.registerDatabase(
                taskProcessDatabase = new TaskProcessDatabase(this)
        );
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetTask 加载完毕");
    }

    private void registerBuiltInTasks() {
        TaskBreakBlock.register();
        TaskPlaceBlock.register();
        TaskCrafting.register();
        TaskFishing.register();
        TaskSubmitItem.register();
        TaskKill.register();
    }
}
