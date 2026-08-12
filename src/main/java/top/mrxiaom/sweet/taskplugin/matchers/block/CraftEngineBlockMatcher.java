package top.mrxiaom.sweet.taskplugin.matchers.block;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.Block;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;

import java.util.Objects;

public class CraftEngineBlockMatcher implements BlockMatcher {
    private final Key blockId;

    public CraftEngineBlockMatcher(String blockId) {
        this.blockId = Key.of(blockId);
    }

    public Key getBlockId() {
        return blockId;
    }

    @Override
    public boolean match(Block block) {
        if (CraftEngineBlocks.isCustomBlock(block)) {
            ImmutableBlockState state = CraftEngineBlocks.getCustomBlockState(block);
            if (state != null) {
                return blockId.equals(state.owner().value().id());
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CraftEngineBlockMatcher)) return false;
        CraftEngineBlockMatcher that = (CraftEngineBlockMatcher) o;
        return blockId.equals(that.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId);
    }
}
