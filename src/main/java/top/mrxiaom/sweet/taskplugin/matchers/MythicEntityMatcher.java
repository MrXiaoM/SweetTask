package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MythicEntityMatcher)) return false;
        MythicEntityMatcher that = (MythicEntityMatcher) o;
        return Objects.equals(mythicId, that.mythicId) && Objects.equals(mythic, that.mythic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mythicId, mythic);
    }
}
