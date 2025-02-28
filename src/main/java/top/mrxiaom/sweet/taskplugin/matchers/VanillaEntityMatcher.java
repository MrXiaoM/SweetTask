package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

public class VanillaEntityMatcher implements EntityMatcher {
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
