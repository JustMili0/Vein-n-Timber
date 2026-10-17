package net.justmili.vnt.core.registries.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.justmili.vnt.client.BreakEffectsPlayer;
import net.justmili.vnt.core.network.BreakEffectsPacket;

@Environment(EnvType.CLIENT)
public class ClientPacketRegistry {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BreakEffectsPacket.TYPE, (payload, context) -> BreakEffectsPlayer.play(context.client().level, payload));
    }
}