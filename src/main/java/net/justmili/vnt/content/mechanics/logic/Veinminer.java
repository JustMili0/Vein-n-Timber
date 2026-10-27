package net.justmili.vnt.content.mechanics.logic;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.justmili.vnt.config.BreakEffectType;
import net.justmili.vnt.config.Config;
import net.justmili.vnt.core.util.BlockBreaking;
import net.justmili.vnt.core.util.BreakEffectsBatch;
import net.justmili.vnt.core.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class Veinminer {
    public static final int SIZE = Config.maxVeinSize;
    public static final int RADIUS = (int) (SIZE * 1.5f);

    public static boolean canSeeVein(Player player, BlockState state) {
        return !player.isCreative() && state.is(ConventionalBlockTags.ORES) && player.getMainHandItem().isCorrectToolForDrops(state);
    }

    public static boolean requireShift(Player player) {
        return Config.shiftToVeinmine == player.isShiftKeyDown();
    }

    public static void mineOreVein(ServerLevel level, ServerPlayer player, BlockState state, BlockPos pos, boolean wasBroken) {
        if (!wasBroken || !(canSeeVein(player, state) && requireShift(player))) return;

        // Mine ore vein and play effects
        var tool = player.getMainHandItem();
        var ores = BlockBreaking.breadthFirstSearch(level, pos, TagUtil.matchOreTags(state), RADIUS, SIZE);
        var effects = new BreakEffectsBatch(BreakEffectType.VEIN, ores.size());
        for (var ore : ores) {
            if (!level.mayInteract(player, ore)) continue;
            if (!BlockBreaking.canAfford(tool)) break; // Stop if tool durability is <= 1
            BlockBreaking.destroy(level, player, tool, level.getBlockState(ore), pos, ore, Config.veinDropPlace, effects, true);
        }
        BlockBreaking.playEffects(level, effects);
    }
}