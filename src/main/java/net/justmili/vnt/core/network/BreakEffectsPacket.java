package net.justmili.vnt.core.network;

import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.justmili.vnt.VeinAndTimber;
import net.justmili.vnt.config.BreakEffectType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

// Tells every client with the mod installed to play block break effects using client's config instead of server defaults.
// Broken blocks are carried all at once. Positions are sent as offsets from first, and states though a palette
public record BreakEffectsPacket(BreakEffectType effectType, int[] stateIds, long[] positions) implements CustomPacketPayload {
    public static final Type<BreakEffectsPacket> TYPE = new Type<>(VeinAndTimber.asId("block_break_effects"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BreakEffectsPacket> CODEC = CustomPacketPayload.codec(BreakEffectsPacket::write, BreakEffectsPacket::read);

    private static BreakEffectsPacket read(RegistryFriendlyByteBuf buf) {
        var effectType = buf.readEnum(BreakEffectType.class);
        int count = buf.readVarInt();
        // Throw if block count is larger than what's left to read
        if (count < 0 || count > buf.readableBytes()) throw new DecoderException("Invalid block count " + count);

        var positions = new long[count];
        var stateIds = new int[count];
        if (count == 0) return new BreakEffectsPacket(effectType, stateIds, positions);

        int paletteSize = buf.readVarInt();
        if (paletteSize < 1 || paletteSize > count) throw new DecoderException("Invalid palette size " + paletteSize);
        var palette = new int[paletteSize];
        for (int paletteIndex = 0; paletteIndex < paletteSize; paletteIndex++) palette[paletteIndex] = buf.readVarInt();

        long first = buf.readLong();
        int firstX = BlockPos.getX(first);
        int firstY = BlockPos.getY(first);
        int firstZ = BlockPos.getZ(first);
        for (int blockIndex = 0; blockIndex < count; blockIndex++) {
            int xOffset = unZigZag(buf.readVarInt());
            int yOffset = unZigZag(buf.readVarInt());
            int zOffset = unZigZag(buf.readVarInt());
            positions[blockIndex] = BlockPos.asLong(firstX + xOffset, firstY + yOffset, firstZ + zOffset);
            stateIds[blockIndex] = palette[buf.readVarInt()];
        }
        return new BreakEffectsPacket(effectType, stateIds, positions);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(effectType);
        buf.writeVarInt(positions.length);
        if (positions.length == 0) return;

        var palette = new IntArrayList();
        var paletteIndexes = new Int2IntOpenHashMap();
        paletteIndexes.defaultReturnValue(-1);
        var blockPaletteIndexes = new int[stateIds.length];
        for (int blockIndex = 0; blockIndex < stateIds.length; blockIndex++) {
            int paletteIndex = paletteIndexes.get(stateIds[blockIndex]);
            if (paletteIndex < 0) {
                paletteIndex = palette.size();
                paletteIndexes.put(stateIds[blockIndex], paletteIndex);
                palette.add(stateIds[blockIndex]);
            }
            blockPaletteIndexes[blockIndex] = paletteIndex;
        }

        buf.writeVarInt(palette.size());
        for (int paletteIndex = 0; paletteIndex < palette.size(); paletteIndex++) buf.writeVarInt(palette.getInt(paletteIndex));

        long first = positions[0];
        int firstX = BlockPos.getX(first);
        int firstY = BlockPos.getY(first);
        int firstZ = BlockPos.getZ(first);
        buf.writeLong(first);
        for (int blockIndex = 0; blockIndex < positions.length; blockIndex++) {
            buf.writeVarInt(zigZag(BlockPos.getX(positions[blockIndex]) - firstX));
            buf.writeVarInt(zigZag(BlockPos.getY(positions[blockIndex]) - firstY));
            buf.writeVarInt(zigZag(BlockPos.getZ(positions[blockIndex]) - firstZ));
            buf.writeVarInt(blockPaletteIndexes[blockIndex]);
        }
    }

    // Folds negative numbers into small positive ones, so offsets towards either side only take a byte
    private static int zigZag(int value) {
        return (value << 1) ^ (value >> 31);
    }

    private static int unZigZag(int value) {
        return (value >>> 1) ^ -(value & 1);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}