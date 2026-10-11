package net.justmili.vnt.core.registries.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.justmili.vnt.client.renderer.OreVeinOutline;
import net.justmili.vnt.client.renderer.TreeTrunkOutline;
import net.justmili.vnt.core.api.renderer.BlockGroupOutlineContext;

//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//?} else if >= 1.21.11 {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
 *///?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
 *///?}

@Environment(EnvType.CLIENT)
public class ClientEventRegistry {

    public static void init() {
        /*? if >= 26.1 {*/var event = LevelRenderEvents.BEFORE_BLOCK_OUTLINE;/*?} else if >= 1.21.11 {*//*var event = WorldRenderEvents.BEFORE_BLOCK_OUTLINE;*//*?} else {*//*var event = WorldRenderEvents.BLOCK_OUTLINE;*///?}
        event.register((context, outline) -> OreVeinOutline.render(new BlockGroupOutlineContext(context, outline)));
        event.register((context, outline) -> TreeTrunkOutline.render(new BlockGroupOutlineContext(context, outline)));
    }
}