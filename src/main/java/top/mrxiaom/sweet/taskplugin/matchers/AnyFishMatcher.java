package top.mrxiaom.sweet.taskplugin.matchers;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

public class AnyFishMatcher implements FishMatcher {
    public static final AnyFishMatcher INSTANCE = new AnyFishMatcher();
    private AnyFishMatcher() {}
    @Override
    public boolean match(Context<Player> context) {
        return true;
    }
}
