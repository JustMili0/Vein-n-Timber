package net.justmili.vnt.client.renderer;

import net.justmili.vnt.content.mechanics.logic.Timber;
import net.justmili.vnt.core.api.renderer.BlockGroupOutlineContext;
import net.justmili.vnt.core.api.renderer.BlockGroupOutlineRenderer;
import net.justmili.vnt.core.util.BlockBreaking;
import net.justmili.vnt.core.util.TreeUtil;
import net.justmili.vnt.core.util.client.OutlineRGB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TreeTrunkOutline {
    private static BlockPos originCache;
    private static long tickBucketCache;
    private static boolean treeCache;

    // Render white outline around the whole tree trunk when the player looks at a log with an axe in hand
    public static boolean render(BlockGroupOutlineContext group) {
        var player = group.player();
        var level = group.level();
        if (player == null || level == null) return true;

        var state = level.getBlockState(group.blockPos());
        if (!Timber.canSeeTree(player, state)) return true;
        if (!isTree(level, group.blockPos())) return true;

        var color = OutlineRGB.get(Timber.requireShift(player));
        return BlockGroupOutlineRenderer.render(group, player, TreeUtil::isLog, Timber.TRUNK_RADIUS, Timber.TRUNK_SIZE,
            color.a(), color.r(), color.g(), color.b(), true);
    }

    private static boolean isTree(Level level, BlockPos origin) {
        long tickBucket = level.getGameTime() / 10;
        if (!origin.equals(originCache) || tickBucket != tickBucketCache) {
            originCache = origin.immutable();
            tickBucketCache = tickBucket;
            treeCache = TreeUtil.isTree(level, origin, BlockBreaking.breadthFirstSearch(level, origin, TreeUtil::isLog, Timber.TRUNK_RADIUS, Timber.TRUNK_SIZE));
        }
        return treeCache;
    }
}