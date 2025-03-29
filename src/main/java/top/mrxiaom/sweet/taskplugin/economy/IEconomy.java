package top.mrxiaom.sweet.taskplugin.economy;

import org.bukkit.OfflinePlayer;

public interface IEconomy {
    String getName();
    default boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }
    double get(OfflinePlayer player);
    void take(OfflinePlayer player, double money);
}
