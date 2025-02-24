package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

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
}
