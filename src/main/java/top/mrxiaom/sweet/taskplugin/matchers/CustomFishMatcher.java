package top.mrxiaom.sweet.taskplugin.matchers;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomFishMatcher implements FishMatcher {
    private final @Nullable String requireId;
    private final @Nullable Float requireSize;
    public CustomFishMatcher(@Nullable String requireId, @Nullable Float requireSize) {
        this.requireId = requireId;
        this.requireSize = requireSize;
    }

    @Nullable
    public String getRequireId() {
        return requireId;
    }

    @Nullable
    public Float getRequireSize() {
        return requireSize;
    }

    @Override
    public boolean match(Context<Player> context) {
        String id = context.arg(ContextKeys.ID);
        if (id == null) return false;
        if (requireId != null && !requireId.equals(id)) return false;
        Float rawSize = context.arg(ContextKeys.SIZE);
        float size = rawSize == null ? 0.0f : rawSize;
        return requireSize == null || size >= requireSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomFishMatcher that = (CustomFishMatcher) o;
        return Objects.equals(requireId, that.requireId) && Objects.equals(requireSize, that.requireSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requireId, requireSize);
    }
}
