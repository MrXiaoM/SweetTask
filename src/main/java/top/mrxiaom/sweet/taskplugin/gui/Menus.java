package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
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
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractGuisModule;

import java.io.File;
import java.util.function.BiConsumer;

@AutoRegister
public class Menus extends AbstractGuisModule<AbstractModel<?, ?>> {
    public Menus(BukkitPlugin plugin) {
        super(plugin, "[menus]");
    }

    private void saveMenu(File folder, String... names) {
        for (String name : names) {
            plugin.saveResource("menus/" + name, new File(folder, name));
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        for (String path : cfg.getStringList("menus-folder")) {
            File folder = plugin.resolve(path);
            if (!folder.exists()) {
                Util.mkdirs(folder);
                if (path.equals("./menus")) saveMenu(folder,
                        "default.yml", "refresh-daily.yml"
                );
            }
            Util.reloadFolder(folder, false, (id, file) -> loadConfig(this, file, id, this::load));
        }
    }

    private AbstractModel<?, ?> load(Menus parent, ConfigurationSection config, String id) {
        if (config.contains("task-icons")) {
            return MenuModel.load(parent, config, id);
        }
        if (config.contains("refresh-icons")) {
            return MenuRefreshModel.load(parent, config, id);
        }
        return null;
    }

    public Impl<?, ?> create(@NotNull Player player, @NotNull AbstractModel<?, ?> menu) {
        return create(null, player, menu);
    }
    public Impl<?, ?> create(@Nullable IGui parent, @NotNull Player player, @NotNull AbstractModel<?, ?> menu) {
        PlayerCache playerCache = plugin.getDatabase().getTasks(player);
        return new Impl<>(parent, player, menu, playerCache);
    }

    public static Menus inst() {
        return instanceOf(Menus.class);
    }

    public class Impl<T, D extends IMenuData> extends Gui<AbstractModel<T, D>> {
        public final IGui parent;
        public PlayerCache playerCache;
        public final D data;
        protected Impl(@Nullable IGui parent, @NotNull Player player, @NotNull AbstractModel<T, D> model, PlayerCache playerCache) {
            super(player, model);
            this.parent = parent;
            this.playerCache = playerCache;
            this.data = model.createData(player, playerCache);
        }

        public SweetTask getPlugin() {
            return plugin;
        }

        @Override
        public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
            this.data.load();
            super.updateInventory(setItem);
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

            T icon = model.mainIcon(clickedId);
            if (icon != null) {
                if (model.click(this, player, icon, click, slot)) {
                    plugin.getScheduler().runTask(() -> updateInventory(view));
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
