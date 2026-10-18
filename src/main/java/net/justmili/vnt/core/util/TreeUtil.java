package net.justmili.vnt.core.util;

import net.justmili.vnt.core.util.search.SearchAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TreeUtil {

    public static boolean isTree(Level level, BlockPos origin, List<BlockPos> logs) {
        if (isNaturalLeafConnected(level, origin)) return true;
        // Go from furthest first since leaves are likely to be far away from origin
        for (int logIndex = logs.size() - 1; logIndex >= 0; logIndex--) {
            if (isNaturalLeafConnected(level, logs.get(logIndex))) return true;
        }
        return false;
    }

    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static List<BlockPos> getConnectedLeaves(Level level, BlockPos origin, List<BlockPos> logs, int maxSize) {
        var origins = new ArrayList<>(logs);
        origins.add(origin);
        return SearchAlgorithms.SPREAD_SEARCH.biSearch(level, origins, TreeUtil::canSpread, maxSize);
    }

    public static boolean canSpread(BlockState from, BlockState to) {
        if (!isNaturalLeaf(to)) return false;
        if (!from.hasProperty(LeavesBlock.DISTANCE) || !to.hasProperty(LeavesBlock.DISTANCE)) return true;
        return to.getValue(LeavesBlock.DISTANCE) > from.getValue(LeavesBlock.DISTANCE);
    }

    public static boolean isNaturalLeafConnected(Level level, BlockPos center) {
        for (var pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            var state = level.getBlockState(pos);
            if (isNaturalLeaf(state)) return true;
        }
        return false;
    }

    // Include wart blocks to natural leaves because nether trees
    public static boolean isNaturalLeaf(BlockState state) {
        return (state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT) && !state.getValue(LeavesBlock.PERSISTENT)) || state.is(BlockTags.WART_BLOCKS);
    }
}