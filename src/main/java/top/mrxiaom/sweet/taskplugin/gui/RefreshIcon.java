package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.economy.IEconomy;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.ArrayList;
import java.util.List;

public class RefreshIcon {
    public final IEconomy economy;
    public final double money;
    public final LoadedIcon icon;
    public final String tipsNoMoney;

    public RefreshIcon(IEconomy economy, double money, LoadedIcon icon, String tipsNoMoney) {
        this.economy = economy;
        this.money = money;
        this.icon = icon;
        this.tipsNoMoney = tipsNoMoney;
    }

    public ItemStack generateIcon(Player player, EnumTaskType type, PlayerCache playerCache) {
        List<Pair<String, Object>> pairs = new ArrayList<>();
        pairs.add(Pair.of("%money%", money));
        pairs.add(Pair.of("%count%", playerCache.getRefreshCountRemain(type)));
        pairs.add(Pair.of("%current%", playerCache.getRefreshCount(type)));
        pairs.add(Pair.of("%max%", TaskManager.inst().getLimitCount(player, type)));
        IModifier<String> displayModifier = oldName -> Pair.replace(oldName, pairs);
        IModifier<List<String>> loreModifier = oldLore -> {
            List<String> lore = new ArrayList<>();
            for (String s : oldLore) {
                lore.add(Pair.replace(s, pairs));
            }
            return lore;
        };
        return icon.generateIcon(player, displayModifier, loreModifier);
    }

    @Nullable
    public static RefreshIcon load(Menus parent, String parentId, ConfigurationSection section, String key) {
        String economyType = section.getString(key + ".cost.type", "invalid");
        IEconomy economy = parent.plugin.getEconomy(economyType);
        if (economy == null) {
            parent.warn("[menus/" + parentId + "] 刷新图标 " + key + " 的 cost.type (" + economyType + ") 未找到");
            return null;
        }
        double money = section.getDouble(key + ".cost.money", -1);
        if (money < 0) {
            parent.warn("[menus/" + parentId + "] 刷新图标 " + key + " 的 cost.money 无效");
            return null;
        }
        LoadedIcon icon = LoadedIcon.load(section, key);
        String tipsNoMoney = section.getString(key + ".tips.no-money", "");
        return new RefreshIcon(economy, money, icon, tipsNoMoney);
    }
}
