package top.mrxiaom.sweet.taskplugin;
        
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.tasks.*;

public class SweetTask extends BukkitPlugin {
    public static SweetTask getInstance() {
        return (SweetTask) BukkitPlugin.getInstance();
    }

    public SweetTask() {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.sweet.taskplugin.libs")
        );
    }
    TaskProcessDatabase taskProcessDatabase;

    @Override
    protected void beforeEnable() {
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
