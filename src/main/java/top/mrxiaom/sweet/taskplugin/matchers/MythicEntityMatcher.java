package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.Objects;

public class MythicEntityMatcher implements EntityMatcher {
    private final String mythicId;
    private final SweetTask plugin = SweetTask.getInstance();

    public MythicEntityMatcher(String mythicId) {
        this.mythicId = mythicId;
        if (plugin.getMythic() == null) {
            plugin.warn("加载了击杀 MythicMobs 怪物任务 " + mythicId + "，但当前服务端未安装或不支持 MythicMobs");
        }
    }

    public String getMythicId() {
        return mythicId;
    }

    @Override
    public boolean match(LivingEntity entity) {
        IMythic mythic = plugin.getMythic();
        if (mythic == null) {
            return false;
        }
        return mythicId.equals(mythic.getMobType(entity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MythicEntityMatcher)) return false;
        MythicEntityMatcher that = (MythicEntityMatcher) o;
        return Objects.equals(mythicId, that.mythicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mythicId);
    }
}
