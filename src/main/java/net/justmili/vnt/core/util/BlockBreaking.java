package net.justmili.vnt.core.util;

import net.justmili.vnt.config.DropPlacements;
import net.justmili.vnt.core.util.search.SearchAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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

    // Drop the block's resources where dropPlace says, origin is the block the player actually broke
    public static void drop(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, BlockPos pos, DropPlacements dropPlace, BlockPos origin) {
        if (dropPlace == DropPlacements.WHERE_BROKEN) {
            Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, tool);
            return;
        }

        var dropPos = dropPlace.posByPlace(pos, origin, player);
        for (var stack : Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool)) {
            if (dropPlace == DropPlacements.PLAYER_INV) player.getInventory().add(stack); // Leaves whatever didn't fit in the stack
            if (!stack.isEmpty()) Block.popResource(level, dropPos, stack);
        }
    }

    public static void destroy(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, DropPlacements dropPlace, BlockPos origin, BlockPos pos, boolean particles, float soundPercentage, int index, boolean damageTool) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        BlockBreaking.drop(level, player, tool, state, pos, dropPlace, origin);
        state.spawnAfterBreak(level, pos, tool, true);
        BlockBreaking.playEffects(level, state, pos, particles, soundPercentage, index);

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

    private static void playEffects(ServerLevel level, BlockState state, BlockPos pos, boolean particles, float soundPercentage, int index) {
        boolean sound = soundPercentage > 0f && Maths.ceil((index + 1) * soundPercentage) > Maths.ceil(index * soundPercentage);

        if (particles && sound) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        } else if (sound) {
            var type = state.getSoundType();
            level.playSound(null, pos, type.getBreakSound(), SoundSource.BLOCKS, Maths.lerp(type.getVolume(), 1f, 0.5f), type.getPitch() * 0.8f);
        } else if (particles) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public static List<BlockPos> breadthFirstSearch(Level level, BlockPos origin, Predicate<BlockState> stateMatch, int maxRadius, int maxSize) {
        return SearchAlgorithms.BREADTH_FIRST_SEARCH.search(level, origin, stateMatch, maxRadius, maxSize);
    }
}