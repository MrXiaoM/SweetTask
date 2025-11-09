package top.mrxiaom.sweet.taskplugin.matchers;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

public interface FishMatcher {

    boolean match(Context<Player> context);

    @Nullable
    static FishMatcher of(String s) {
        if (s.equalsIgnoreCase("ANY")) return AnyFishMatcher.INSTANCE;
        if (s.contains("@")) {
            String[] split = s.split("@", 2);
            String id = split[0].equalsIgnoreCase("ANY") ? null : split[0];
            Float requireSize = Util.parseFloat(split[1]).orElse(null);
            if (requireSize == null) return null;
            return new CustomFishMatcher(id, requireSize);
        }
        return new CustomFishMatcher(s, null);
    }
}
