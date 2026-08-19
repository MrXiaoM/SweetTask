package top.mrxiaom.sweet.taskplugin.matchers.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;

import java.util.Objects;

public class VanillaEntityMatcher implements EntityMatcher {
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable EntityMatcher parse(@NonNull String input) {
            EntityType entityType = Util.valueOr(EntityType.class, input, null);
            if (entityType != null) {
                return new VanillaEntityMatcher(entityType);
            }
            return null;
        }
        @Override
        public int getPriority() {
            return 2000;
        }
    };
    private final EntityType type;

    public VanillaEntityMatcher(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    @Override
    public boolean match(LivingEntity entity) {
        return type.equals(entity.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaEntityMatcher)) return false;
        VanillaEntityMatcher that = (VanillaEntityMatcher) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
