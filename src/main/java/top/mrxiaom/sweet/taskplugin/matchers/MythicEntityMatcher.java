package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;

public class MythicEntityMatcher implements EntityMatcher {
    private final String mythicId;

    public MythicEntityMatcher(String mythicId) {
        this.mythicId = mythicId;
    }

    public String getMythicId() {
        return mythicId;
    }

    @Override
    public boolean match(LivingEntity entity) {
        // TODO: 判定 MythicMobs 生物
        return false;
    }
}
