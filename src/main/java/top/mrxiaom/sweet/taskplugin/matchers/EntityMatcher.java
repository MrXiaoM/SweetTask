package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public interface EntityMatcher {

    boolean match(LivingEntity entity);

    @Nullable
    static EntityMatcher of(String s) {
        for (Provider provider : SweetTask.getInstance().entityMatchers().all()) {
            EntityMatcher matcher = provider.parse(s);
            if (matcher != null) {
                return matcher;
            }
        }
        return null;
    }

    interface Provider extends WithPriority {
        @Nullable EntityMatcher parse(@NotNull String input);
    }
}
