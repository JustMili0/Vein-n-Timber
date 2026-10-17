package net.justmili.vnt.core.registries;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.justmili.vnt.core.network.BreakEffectsPacket;

public class PacketRegistry {

    public static void init() {
        PayloadTypeRegistry./*? if >=26.1 {*/clientboundPlay()/*?} else {*//*playS2C()*//*?}*/.register(BreakEffectsPacket.TYPE, BreakEffectsPacket.CODEC);
    }
}