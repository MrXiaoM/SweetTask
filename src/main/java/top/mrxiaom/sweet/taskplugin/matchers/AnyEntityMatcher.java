package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.entity.LivingEntity;

public class AnyEntityMatcher implements EntityMatcher {
    public static final AnyEntityMatcher INSTANCE = new AnyEntityMatcher();
    private AnyEntityMatcher() {}
    @Override
    public boolean match(LivingEntity block) {
        return true;
    }
}
