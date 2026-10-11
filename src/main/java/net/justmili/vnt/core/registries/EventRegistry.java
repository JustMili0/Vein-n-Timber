package net.justmili.vnt.core.registries;

import net.justmili.vnt.content.mechanics.logic.Timber;
import net.justmili.vnt.content.mechanics.logic.Veinminer;
import net.justmili.vnt.core.api.event.server.BlockBreakEvent;

public class EventRegistry {

    public static void init() {
        BlockBreakEvent.BLOCK_BROKEN.register(Veinminer::mineOreVein);
        BlockBreakEvent.BLOCK_BROKEN.register(Timber::chopDownTree);
    }
}