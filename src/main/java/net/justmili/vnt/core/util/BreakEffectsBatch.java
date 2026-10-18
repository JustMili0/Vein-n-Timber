package net.justmili.vnt.core.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.justmili.vnt.config.BreakEffectType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// Gets broken blocks all at once to send with one packet instead of multiple
public class BreakEffectsBatch {
    private final BreakEffectType effectType;
    private final LongArrayList positions;
    private final IntArrayList stateIds;

    public BreakEffectsBatch(BreakEffectType effectType, int expectedSize) {
        this.effectType = effectType;
        this.positions = new LongArrayList(expectedSize);
        this.stateIds = new IntArrayList(expectedSize);
    }

    public void add(BlockPos pos, BlockState state) {
        positions.add(pos.asLong());
        stateIds.add(Block.getId(state));
    }

    public BreakEffectType effectType() {
        return effectType;
    }

    public long[] positions() {
        return positions.toLongArray();
    }

    public int[] stateIds() {
        return stateIds.toIntArray();
    }
}