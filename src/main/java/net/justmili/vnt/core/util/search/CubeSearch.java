package net.justmili.vnt.core.util.search;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Searches every block within a cube around the origin.
 * Matching blocks are returned closest to the origin first.
 * <p>
 * Blocks don't have to be connected, and origin IS included in the result if it matches.
 * Every block in the cube gets checked, so keep the radius small as block count grows cubically.
 */
public class CubeSearch implements SearchAlgorithm {

    @Override
    public List<BlockPos> search(Level level, BlockPos origin, Predicate<BlockState> stateMatch, int maxRadius, int maxSize) {
        if (maxSize <= 0) return List.of();

        var nearestFirst = Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(origin));
        var farthestFirst = nearestFirst.reversed();
        var nearest = new PriorityQueue<>(farthestFirst);

        for (var pos : withinClosed(origin, maxRadius)) {
            if (!stateMatch.test(level.getBlockState(pos))) continue;
            var current = pos.immutable();

            if (nearest.size() < maxSize) {
                nearest.add(current);
            } else if (nearestFirst.compare(current, nearest.peek()) < 0) {
                nearest.poll();
                nearest.add(current);
            }
        }

        var found = new ArrayList<>(nearest);
        found.sort(nearestFirst);
        return found;
    }

    @Override
    public List<BlockPos> biSearch(Level level, Collection<BlockPos> origins, BiPredicate<BlockState, BlockState> canSpread, int maxSize) {
        return List.of();
    }
}