package top.mrxiaom.sweet.taskplugin.economy;

import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

public class PlayerPointsEconomy implements IEconomy {
    PlayerPointsAPI impl;
    public PlayerPointsEconomy(PlayerPointsAPI impl) {
        this.impl = impl;
    }
    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public double get(OfflinePlayer player) {
        return impl.look(player.getUniqueId());
    }

    @Override
    public void take(OfflinePlayer player, double money) {
        impl.take(player.getUniqueId(), (int) Math.round(money));
    }
}
