package net.justmili.vnt.content.mechanics.logic;

import net.justmili.vnt.config.Config;
import net.justmili.vnt.config.DropPlacements;
import net.justmili.vnt.core.util.BlockBreaking;
import net.justmili.vnt.core.util.TreeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class Timber {
    public static int TRUNK_SIZE = Config.maxTrunkSize;
    public static int TRUNK_RADIUS = Config.maxTrunkRadius;
    public static int LEAVES_SIZE = Config.maxLeavesSize;

    public static boolean canSeeTree(Player player, BlockState state) {
        return !player.isCreative() && TreeUtil.isLog(state) && player.getMainHandItem().is(ItemTags.AXES);
    }

    public static boolean requireShift(Player player) {
        return Config.shiftToTimber == player.isShiftKeyDown();
    }

    public static void chopDownTree(ServerLevel level, ServerPlayer player, BlockState state, BlockPos pos, boolean wasBroken) {
        if (!wasBroken || !(canSeeTree(player, state) && requireShift(player))) return;

        var logs = BlockBreaking.breadthFirstSearch(level, pos, TreeUtil::isLog, TRUNK_RADIUS, TRUNK_SIZE);
        if (!TreeUtil.isTree(level, pos, logs)) return;
        var leaves = TreeUtil.getConnectedLeaves(level, pos, logs, LEAVES_SIZE);

        var tool = player.getMainHandItem();
        boolean trunkParticles = Config.trunkBreakParticles;
        boolean leavesParticles = Config.leavesBreakParticles;
        float trunkSounds = Config.trunkBreakSoundsPercentage;
        float leavesSounds = Config.leavesBreakSoundPercentage;
        boolean leavesDamageTool = Config.leavesDamageTool;
        var trunkDropPlace = Config.trunkDropPlace;
        var leavesDropPlace = Config.leavesDropPlace;
        if (!Config.includeLeavesDrops
            && (leavesDropPlace == DropPlacements.PLAYER_POS
            || leavesDropPlace == DropPlacements.PLAYER_INV)
        ) leavesDropPlace = DropPlacements.WHERE_BROKEN;

        // Chop tree
        int logIndex = 0;
        for (var log : logs) {
            if (!level.mayInteract(player, log)) continue;
            if (!BlockBreaking.canAfford(tool)) break; // Stop if tool durability is <= 1
            BlockBreaking.destroy(level, player, tool, level.getBlockState(log), trunkDropPlace, pos, log, trunkParticles, trunkSounds, logIndex++, true);
        }
        if (Config.breakTreeLeaves) {
            int leafIndex = 0;
            for (var leaf : leaves) {
                if (!level.mayInteract(player, leaf)) continue;
                if (!BlockBreaking.canAfford(tool) && leavesDamageTool) break; // Stop if tool durability is <= 1
                BlockBreaking.destroy(level, player, tool, level.getBlockState(leaf), leavesDropPlace, pos, leaf, leavesParticles, leavesSounds, leafIndex++, leavesDamageTool);
            }
        }
    }
}