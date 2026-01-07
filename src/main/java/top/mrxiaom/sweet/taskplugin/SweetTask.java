package top.mrxiaom.sweet.taskplugin;
        
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SweetTask extends BukkitPlugin {
    public static boolean DEBUG = BuildConstants.IS_DEVELOPMENT_BUILD;
    public static SweetTask getInstance() {
        return (SweetTask) BukkitPlugin.getInstance();
    }

    public SweetTask() throws Exception {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.taskplugin.libs")
        );
        scheduler = new FoliaLibScheduler(this);

        getLogger().info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
        for (String key : overrideLibraries.getKeys(false)) {
            resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
        }
        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        getLogger().info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
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
        if (DEBUG) {
            info("调试模式已开启");
        }
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
        info("SweetTask 加载完毕");
        if (BuildConstants.IS_DEVELOPMENT_BUILD) {
            warn("你正在使用开发版本的 SweetTask，遇到漏洞请通过 Github 反馈!");
            warn("https://github.com/MrXiaoM/SweetTask/issues");
        }
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        if (!BuildConstants.IS_DEVELOPMENT_BUILD) {
            DEBUG = config.getBoolean("debug", false);
            if (DEBUG) {
                info("调试模式已开启");
            }
        }
    }

    private void registerBuiltInTasks() {
        TaskBreakBlock.register();
        TaskPlaceBlock.register();
        TaskCrafting.register();
        TaskFishing.register();
        TaskSubmitItem.register();
        TaskKill.register();
        if (Bukkit.getPluginManager().isPluginEnabled("CustomFishing")) {
            TaskCustomFishing.register();
        }
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
            PlayerPointsEconomy economy = new PlayerPointsEconomy(impl);
            economies.put("PlayerPoints", economy);
            economies.put("PLAYER_POINTS", economy);
        }
    }
}
