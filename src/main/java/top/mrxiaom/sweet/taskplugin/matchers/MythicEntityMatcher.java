package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

public class MythicEntityMatcher implements EntityMatcher {
    private final String mythicId;
    private final IMythic mythic = SweetTask.getInstance().getMythic();

    public MythicEntityMatcher(String mythicId) {
        this.mythicId = mythicId;
    }

    public String getMythicId() {
        return mythicId;
    }

    @Override
    public boolean match(LivingEntity entity) {
        return mythicId.equals(mythic.getMobType(entity));
    }
}
