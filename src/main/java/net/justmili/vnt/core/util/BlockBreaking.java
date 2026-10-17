package net.justmili.vnt.core.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.justmili.vnt.config.BreakEffectType;
import net.justmili.vnt.config.DropPlacements;
import net.justmili.vnt.core.network.BreakEffectsPacket;
import net.justmili.vnt.core.util.search.SearchAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.function.Predicate;

public class BlockBreaking {
    private static final double EFFECTS_RANGE = 64;

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

    public static void destroy(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, DropPlacements dropPlace, BlockPos origin, BlockPos pos, BreakEffectType effectGroup, int index, boolean damageTool) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        BlockBreaking.drop(level, player, tool, state, pos, dropPlace, origin);
        state.spawnAfterBreak(level, pos, tool, true);
        BlockBreaking.playEffects(level, state, pos, effectGroup, index);

        level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS);
        if (damageTool) {
            tool.mineBlock(level, state, pos, player);
        } else {
            // Award stat for breaking even tho tool isn't used
            player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
        }
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
    }

    private static void playEffects(ServerLevel level, BlockState state, BlockPos pos, BreakEffectType effects, int index) {
        boolean particles = effects.particles();
        boolean sounds = effects.playAt(index);
        int stateId = Block.getId(state);
        var payload = new BreakEffectsPacket(pos, stateId, effects, index);
        var sound = state.getSoundType();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (var player : level.players()) {
            if (player.distanceToSqr(x, y, z) > EFFECTS_RANGE * EFFECTS_RANGE) continue;

            // Clients with the mod play the effects with their own config, the rest get the server defaults
            if (ServerPlayNetworking.canSend(player, BreakEffectsPacket.TYPE)) {
                ServerPlayNetworking.send(player, payload);
            } else if (particles && sounds) {
                // LevelEvent constant name changed in 26.3 so it's just better to use the numerical value
                player.connection.send(new ClientboundLevelEventPacket(2001, pos, stateId, false));
            } else if (sounds) {
                sendBreakSoundPacket(player, level, sound, pos);
            } else if (particles) {
                level.sendParticles(player, new BlockParticleOption(ParticleTypes.BLOCK, state), false, /*? if >=1.21.4 {*/false, /*?}*/x, y, z, 20, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    public static List<BlockPos> breadthFirstSearch(Level level, BlockPos origin, Predicate<BlockState> stateMatch, int maxRadius, int maxSize) {
        return SearchAlgorithms.BREADTH_FIRST_SEARCH.search(level, origin, stateMatch, maxRadius, maxSize);
    }

    public static void sendBreakSoundPacket(ServerPlayer player, ServerLevel level, SoundType sound, BlockPos pos) {
        player.connection.send(new ClientboundSoundPacket(
            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getBreakSound()), SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(),
            Sounds.randomVolume(sound), Sounds.randomPitch(sound), level.getRandom().nextLong()
        ));
    }
}