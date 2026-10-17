package net.justmili.vnt.client;

import net.fabricmc.api.ClientModInitializer;
import net.justmili.vnt.config.Config;
import net.justmili.vnt.core.registries.client.ClientEventRegistry;
import net.justmili.vnt.core.registries.client.ClientPacketRegistry;

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Config.initClient();
        ClientPacketRegistry.init();
        ClientEventRegistry.init();
    }
}