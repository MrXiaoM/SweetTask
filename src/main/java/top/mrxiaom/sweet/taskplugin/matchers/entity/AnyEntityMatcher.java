package top.mrxiaom.sweet.taskplugin.matchers.entity;

import org.bukkit.entity.LivingEntity;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;

public class AnyEntityMatcher implements EntityMatcher {
    public static final AnyEntityMatcher INSTANCE = new AnyEntityMatcher();
    private AnyEntityMatcher() {}
    @Override
    public boolean match(LivingEntity block) {
        return true;
    }
}
