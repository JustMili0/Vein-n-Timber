package net.justmili.vnt.core.network;

import net.justmili.vnt.VeinAndTimber;
import net.justmili.vnt.config.BreakEffectType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

// Tells clients that have the mod to play block break effects using client's common config instead of server's
public record BreakEffectsPacket(BlockPos pos, int stateId, BreakEffectType effectType, int index) implements CustomPacketPayload {
    public static final Type<BreakEffectsPacket> TYPE = new Type<>(VeinAndTimber.asId("block_break_effects"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BreakEffectsPacket> CODEC = CustomPacketPayload.codec(BreakEffectsPacket::write, BreakEffectsPacket::new);

    private BreakEffectsPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readInt(), buf.readEnum(BreakEffectType.class), buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(stateId);
        buf.writeEnum(effectType);
        buf.writeInt(index);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}