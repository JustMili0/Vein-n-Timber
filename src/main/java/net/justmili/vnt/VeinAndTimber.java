package net.justmili.vnt;

import net.fabricmc.api.ModInitializer;
import net.justmili.vnt.config.Config;
import net.justmili.vnt.core.registries.EventRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeinAndTimber implements ModInitializer {
    public static final String ID = "vein_n_timber";
    public static final String NAME = "Vein n' Timber";
    public static final String BUILD = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(VeinAndTimber.class);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {} ({}) version {}", NAME, ID, BUILD);
        Config.initCommon();
        EventRegistry.init();
    }
}