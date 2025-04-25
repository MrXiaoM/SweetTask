package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.HashMap;
import java.util.Map;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class MenuRefreshModel extends AbstractModel<RefreshIcon, MenuRefreshModel.Data> implements IMenuCondition {
    public final EnumTaskType refreshType;
    public final String refreshTips;
    public MenuRefreshModel(String id, String title, char[] inventory, String permission, EnumTaskType refreshType, String refreshTips,
                            Map<Character, RefreshIcon> refreshIcons, Map<Character, LoadedIcon> otherIcons
    ) {
        super(id, title, inventory, permission, refreshIcons, otherIcons);
        this.refreshType = refreshType;
        this.refreshTips = refreshTips;
    }

    @Override
    public boolean check(Player player) {
        PlayerCache playerCache = SweetTask.getInstance().getDatabase().getTasks(player);
        Boolean refresh = playerCache.canRefresh(refreshType);
        return refresh != null && refresh;
    }

    @Override
    public ItemStack applyMainIcon(
            Menus.Impl<RefreshIcon, Data> gui, Player player,
            char id, int index, int appearTimes
    ) {
        RefreshIcon icon = mainIcon(id);
        if (icon != null) {
            return icon.generateIcon(player, refreshType, gui.playerCache);
        }
        LoadedIcon otherIcon = otherIcon(id);
        if (otherIcon != null) {
            return otherIcon.generateIcon(player);
        }
        return null;
    }

    @Override
    public Data createData(Player player, PlayerCache playerCache) {
        return new Data();
    }

    public static class Data implements IMenuData {
        @Override
        public void load() {
        }
    }

    @Override
    public boolean click(
            Menus.Impl<RefreshIcon, Data> gui, Player player,
            RefreshIcon icon, ClickType click, int invSlot
    ) {
        gui.setClickLock(true);
        Boolean refresh = gui.playerCache.canRefresh(refreshType);
        if (refresh == null) {
            t(player, "&e你已经完成过任务了，不可刷新");
            player.closeInventory();
            return false;
        }
        if (!refresh) {
            t(player, "&e你的刷新次数已耗尽");
            player.closeInventory();
            return false;
        }
        if (!icon.economy.has(player, icon.money)) {
            if (!icon.tipsNoMoney.isEmpty()) {
                AdventureUtil.sendMessage(player, icon.tipsNoMoney);
            }
            return false;
        }
        icon.economy.take(player, icon.money);
        gui.playerCache.submitRefresh(refreshType, () -> {
            if (!refreshTips.isEmpty()) {
                AdventureUtil.sendMessage(player, refreshTips);
            }
            if (gui.parent != null) {
                gui.parent.open();
            } else {
                player.closeInventory();
            }
            gui.setClickLock(false);
        });
        return false;
    }

    public static MenuRefreshModel load(Menus parent, ConfigurationSection config, String id) {
        ConfigurationSection section;
        String title = config.getString("title", "").replace("%id%", id);
        String permission = config.getString("permission", "").replace("%id%", id);
        char[] inventory = String.join("", config.getStringList("inventory")).toCharArray();
        EnumTaskType refreshType = Util.valueOr(EnumTaskType.class, config.getString("refresh-type"), null);
        if (refreshType == null) {
            parent.warn("[menus/" + id + "] 刷新类型输入有误");
            return null;
        }
        String refreshTips = config.getString("refresh-tips", "");

        Map<Character, RefreshIcon> refreshIcons = new HashMap<>();
        section = config.getConfigurationSection("refresh-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            if (key.length() > 1) {
                parent.warn("[menus/" + id + "] 刷新图标 " + key + " 的名称过长，请改为一个字符");
                continue;
            }
            char iconId = key.charAt(0);
            RefreshIcon loaded = RefreshIcon.load(parent, id, section, key);
            if (loaded != null) {
                refreshIcons.put(iconId, loaded);
            }
        }

        Map<Character, LoadedIcon> otherIcons = new HashMap<>();
        section = config.getConfigurationSection("other-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            if (key.length() > 1) {
                parent.warn("[menus/" + id + "] 额外图标 " + key + " 的名称过长，请改为一个字符");
                continue;
            }
            char iconId = key.charAt(0);
            LoadedIcon loaded = LoadedIcon.load(section, key);
            otherIcons.put(iconId, loaded);
        }
        return new MenuRefreshModel(id, title, inventory, permission.isEmpty() ? null : permission,
                refreshType, refreshTips, refreshIcons, otherIcons);
    }
}
