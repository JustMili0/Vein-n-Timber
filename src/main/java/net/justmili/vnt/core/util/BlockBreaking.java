package net.justmili.vnt.core.util;

import net.justmili.vnt.core.util.search.SearchAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.function.Predicate;

public class BlockBreaking {

    public static boolean canAfford(ItemStack tool) {
        if (!tool.isDamageableItem()) return true;
        return tool.getMaxDamage() - tool.getDamageValue() > 1;
    }

    // TODO: Do some fuckery (probably mixins) to allow this to not play all block breaking sounds but only a percentage of them
    public static void destroy(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, BlockPos pos, boolean playEffects, boolean damageTool) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, tool);
        state.spawnAfterBreak(level, pos, tool, true);
        if (playEffects) level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));

        level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        if (damageTool) {
            tool.mineBlock(level, state, pos, player);
        } else {
            // Award stat for breaking even tho tool isn't used
            player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
        }
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
    }

    public static void destroy(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, BlockPos pos, boolean playEffects) {
        BlockBreaking.destroy(level, player, tool, state, pos, playEffects, true);
    }

    public static List<BlockPos> breadthFirstSearch(Level level, BlockPos origin, Predicate<BlockState> stateMatch, int maxRadius, int maxSize) {
        return SearchAlgorithms.BREADTH_FIRST_SEARCH.search(level, origin, stateMatch, maxRadius, maxSize);
    }
}