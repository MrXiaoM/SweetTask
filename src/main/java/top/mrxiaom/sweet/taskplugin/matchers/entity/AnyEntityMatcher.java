package top.mrxiaom.sweet.taskplugin.matchers.entity;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;

public class AnyEntityMatcher implements EntityMatcher {
    public static final AnyEntityMatcher INSTANCE = new AnyEntityMatcher();
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable EntityMatcher parse(@NotNull String input) {
            if (input.equalsIgnoreCase("ANY")) {
                return INSTANCE;
            }
            return null;
        }
        @Override
        public int getPriority() {
            return 0;
        }
    };
    private AnyEntityMatcher() {}
    @Override
    public boolean match(LivingEntity block) {
        return true;
    }
}
