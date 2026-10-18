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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockBreaking {
    private static final double EFFECTS_RANGE = 64;

    public static boolean canAfford(ItemStack tool) {
        if (!tool.isDamageableItem()) return true;
        return tool.getMaxDamage() - tool.getDamageValue() > 1;
    }

    // Drop block resources accordingly to config
    public static void drop(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, BlockPos origin, BlockPos pos, DropPlacements dropPlace) {
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

    public static void destroy(ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, BlockPos origin, BlockPos pos, DropPlacements dropPlace, BreakEffectsBatch effects, boolean damageTool) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        BlockBreaking.drop(level, player, tool, state, origin, pos, dropPlace);
        state.spawnAfterBreak(level, pos, tool, true);
        effects.add(pos, state);

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

    // Send the effects of every block in the batch at once, one packet per client with the mod instead of one per block
    public static void playEffects(ServerLevel level, BreakEffectsBatch effects) {
        var effectType = effects.effectType();
        var positions = effects.positions();
        if (positions.length == 0) return;
        var stateIds = effects.stateIds();

        BreakEffectsPacket packet = null;
        var vanillaPlayers = new ArrayList<ServerPlayer>();
        for (var player : level.players()) {
            // Clients with the mod play the effects with their own config, the rest get the server defaults
            if (!ServerPlayNetworking.canSend(player, BreakEffectsPacket.TYPE)) {
                vanillaPlayers.add(player);
            } else if (isInEffectsRange(player, positions)) {
                if (packet == null) packet = new BreakEffectsPacket(effectType, stateIds, positions);
                ServerPlayNetworking.send(player, packet);
            }
        }

        if (!vanillaPlayers.isEmpty()) playVanillaEffects(level, vanillaPlayers, effectType, positions, stateIds);
    }

    private static boolean isInEffectsRange(ServerPlayer player, long[] positions) {
        double rangeSqr = EFFECTS_RANGE * EFFECTS_RANGE;
        for (var position : positions) {
            if (player.distanceToSqr(BlockPos.getX(position), BlockPos.getY(position), BlockPos.getZ(position)) <= rangeSqr) return true;
        }
        return false;
    }

    private static void playVanillaEffects(ServerLevel level, List<ServerPlayer> players, BreakEffectType effectType, long[] positions, int[] stateIds) {
        boolean particles = effectType.particles();
        double rangeSqr = EFFECTS_RANGE * EFFECTS_RANGE;

        for (int index = 0; index < positions.length; index++) {
            boolean sounds = effectType.playAt(index);
            if (!particles && !sounds) continue;

            var pos = BlockPos.of(positions[index]);
            var state = Block.stateById(stateIds[index]);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            // Build packet once, it's the same for everyone
            Packet<?> packet = null;
            if (particles && sounds) {
                // LevelEvent constant name changed in 26.3 so it's just better to use the numerical value
                packet = new ClientboundLevelEventPacket(2001, pos, stateIds[index], false);
            } else if (sounds) {
                var sound = state.getSoundType();
                packet = new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getBreakSound()), SoundSource.BLOCKS, x, y, z,
                    Sounds.randomVolume(sound), Sounds.randomPitch(sound), level.getRandom().nextLong()
                );
            }
            var particle = packet == null? new BlockParticleOption(ParticleTypes.BLOCK, state) : null;

            for (var player : players) {
                if (player.distanceToSqr(x, y, z) > rangeSqr) continue;

                if (packet != null) {
                    player.connection.send(packet);
                } else {
                    level.sendParticles(player, particle, false, /*? if >=1.21.4 {*/false, /*?}*/x, y, z, 20, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
    }

    public static List<BlockPos> breadthFirstSearch(Level level, BlockPos origin, Predicate<BlockState> stateMatch, int maxRadius, int maxSize) {
        return SearchAlgorithms.BREADTH_FIRST_SEARCH.search(level, origin, stateMatch, maxRadius, maxSize);
    }
}