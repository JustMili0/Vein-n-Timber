package net.justmili.vnt.core.api.event.server;

import net.justmili.vnt.core.api.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

// Temporary event class, to be replaced with Core Libs' player block break event once it's made
public class BlockBreakEvent {
    private BlockBreakEvent() {
    }

    public static final Event<BlockBroken> BLOCK_BROKEN = Event.create(BlockBroken.class, callbacks -> (level, player, state, pos, wasBroken) -> {
        for (var callback : callbacks) callback.onBlockBroken(level, player, state, pos, wasBroken);
    });

    @FunctionalInterface
    public interface BlockBroken {
        void onBlockBroken(ServerLevel level, ServerPlayer player, BlockState state, BlockPos pos, boolean wasBroken);
    }
}