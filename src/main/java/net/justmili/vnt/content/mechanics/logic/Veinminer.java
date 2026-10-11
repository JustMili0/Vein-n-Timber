package net.justmili.vnt.content.mechanics.logic;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.justmili.vnt.config.Config;
import net.justmili.vnt.core.util.BlockBreaking;
import net.justmili.vnt.core.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class Veinminer {
    public static final int SIZE = Config.maxVeinSize;
    public static final int RADIUS = Config.maxVeinRadius;

    public static boolean canSeeVein(Player player, BlockState state) {
        return !player.isCreative() && state.is(ConventionalBlockTags.ORES) && player.getMainHandItem().isCorrectToolForDrops(state);
    }

    public static boolean requireShift(Player player) {
        return Config.shiftToVeinmine == player.isShiftKeyDown();
    }

    public static void mineOreVein(ServerLevel level, ServerPlayer player, BlockState state, BlockPos pos, boolean wasBroken) {
        if (!wasBroken || !(canSeeVein(player, state) && requireShift(player))) return;

        // Mine ore vein
        var tool = player.getMainHandItem();
        for (var target : BlockBreaking.breadthFirstSearch(level, pos, TagUtil.matchOreTags(state), RADIUS, SIZE)) {
            if (!BlockBreaking.canAfford(tool)) break; // Stop if tool durability is <= 1
            BlockBreaking.destroy(level, player, tool, level.getBlockState(target), Config.veinDropPlace, pos, target, Config.veinBreakParticles, true);
        }
    }
}