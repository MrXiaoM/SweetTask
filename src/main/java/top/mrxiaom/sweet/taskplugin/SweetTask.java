package top.mrxiaom.sweet.taskplugin;
        
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.sweet.taskplugin.actions.ActionBack;
import top.mrxiaom.sweet.taskplugin.actions.ActionOpenGui;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.economy.IEconomy;
import top.mrxiaom.sweet.taskplugin.economy.PlayerPointsEconomy;
import top.mrxiaom.sweet.taskplugin.economy.VaultEconomy;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;
import top.mrxiaom.sweet.taskplugin.mythic.Mythic4;
import top.mrxiaom.sweet.taskplugin.mythic.Mythic5;
import top.mrxiaom.sweet.taskplugin.tasks.*;

import java.util.Map;
import java.util.TreeMap;

public class SweetTask extends BukkitPlugin {
    public static boolean DEBUG = false;
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
    Map<String, IEconomy> economies = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    IMythic mythic;

    public IMythic getMythic() {
        return mythic;
    }

    @Nullable
    public IEconomy getEconomy(String name) {
        return economies.get(name);
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
        ActionProviders.registerActionProvider(ActionOpenGui.PROVIDER);
        ActionProviders.registerActionProvider(ActionBack.PROVIDER);
        registerBuiltInTasks();
        loadEconomyProviders();
        options.registerDatabase(
                taskProcessDatabase = new TaskProcessDatabase(this)
        );
    }

    @Override
    protected void afterEnable() {
        if (PAPI.isEnabled()) {
            new Placeholders(this).register();
        }
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

    private static boolean has(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T service(Class<T> type) {
        RegisteredServiceProvider<T> provider = Bukkit.getServicesManager().getRegistration(type);
        if (provider != null) {
            return provider.getProvider();
        }
        return null;
    }

    private void loadEconomyProviders() {
        economies.clear();
        if (has("Vault")) {
            Economy impl = service(Economy.class);
            economies.put("Vault", new VaultEconomy(impl));
        }
        if (has("PlayerPoints")) {
            PlayerPointsAPI impl = PlayerPoints.getInstance().getAPI();
            economies.put("PlayerPoints", new PlayerPointsEconomy(impl));
        }
    }
}
