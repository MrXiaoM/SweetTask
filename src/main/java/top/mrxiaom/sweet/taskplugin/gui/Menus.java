package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractGuisModule;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.io.File;
import java.util.List;

@AutoRegister
public class Menus extends AbstractGuisModule<MenuModel> {
    public Menus(BukkitPlugin plugin) {
        super(plugin, "[menus]");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        for (String path : cfg.getStringList("menus-folder")) {
            File folder = plugin.resolve(path);
            if (!folder.exists()) {
                Util.mkdirs(folder);
                if (path.equals("./menus")) {
                    plugin.saveResource("menus/default.yml", new File(folder, "default.yml"));
                }
            }
            Util.reloadFolder(folder, false, (id, file) -> loadConfig(this, file, id, MenuModel::load));
        }
    }

    public Impl create(Player player, MenuModel menu) {
        PlayerCache playerCache = plugin.getDatabase().getTasks(player);
        return new Impl(player, menu, playerCache);
    }

    public static Menus inst() {
        return instanceOf(Menus.class);
    }

    public class Impl extends Gui<MenuModel> {
        public PlayerCache playerCache;
        public List<Pair<LoadedTask, TaskCache>> tasksDaily, tasksWeekly, tasksMonthly;
        protected Impl(@NotNull Player player, @NotNull MenuModel model, PlayerCache playerCache) {
            super(player, model);
            this.setPlayerCache(playerCache);
        }

        public SweetTask getPlugin() {
            return plugin;
        }

        public void setPlayerCache(PlayerCache playerCache) {
            this.playerCache = playerCache;
            if (tasksDaily != null) tasksDaily.clear();
            if (tasksWeekly != null) tasksWeekly.clear();
            if (tasksMonthly != null) tasksMonthly.clear();
            this.tasksDaily = playerCache.getTasksByType(EnumTaskType.DAILY);
            this.tasksWeekly = playerCache.getTasksByType(EnumTaskType.WEEKLY);
            this.tasksMonthly = playerCache.getTasksByType(EnumTaskType.MONTHLY);
        }

        @Nullable
        public Pair<LoadedTask, TaskCache> getTask(EnumTaskType type, int index) {
            if (index < 0) return null;
            List<Pair<LoadedTask, TaskCache>> list;
            switch (type) {
                case DAILY:
                    list = tasksDaily;
                    break;
                case WEEKLY:
                    list = tasksWeekly;
                    break;
                case MONTHLY:
                    list = tasksMonthly;
                    break;
                default:
                    throw new IllegalArgumentException(type.name() + " is not supported.");
            }
            if (index >= list.size()) return null;
            return list.get(index);
        }

        @Override
        public void onClick(
                InventoryAction action, ClickType click,
                InventoryType.SlotType slotType, int slot,
                ItemStack currentItem, ItemStack cursor,
                InventoryView view, InventoryClickEvent event
        ) {
            event.setCancelled(true);
            Character clickedId = getClickedId(slot);
            if (clickedId == null) return;

            TaskIcon icon = model.getTaskIcon(clickedId);
            if (icon != null) {
                if (model.click(this, player, icon, click, slot)) {
                    Bukkit.getScheduler().runTask(plugin, () -> updateInventory(view));
                }
                return;
            }

            LoadedIcon otherIcon = otherIcons.get(clickedId);
            if (otherIcon != null) {
                otherIcon.click(player, click);
            }
        }
    }
}
