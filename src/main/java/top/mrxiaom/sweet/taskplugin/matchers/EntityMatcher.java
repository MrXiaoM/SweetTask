package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

public interface EntityMatcher {

    boolean match(LivingEntity entity);

    @Nullable
    static EntityMatcher of(String s) {
        if (s.startsWith("mythic:")) {
            return new MythicEntityMatcher(s.substring(7));
        }
        EntityType entityType = Util.valueOr(EntityType.class, s, null);
        if (entityType != null) {
            return new VanillaEntityMatcher(entityType);
        }
        return null;
    }
}
