package top.mrxiaom.sweet.taskplugin.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

public class VaultEconomy implements IEconomy {
    Economy impl;
    public VaultEconomy(Economy impl) {
        this.impl = impl;
    }
    @Override
    public String getName() {
        return impl.getName();
    }

    @Override
    public double get(OfflinePlayer player) {
        return impl.getBalance(player);
    }

    @Override
    public void take(OfflinePlayer player, double money) {
        impl.withdrawPlayer(player, money);
    }
}
