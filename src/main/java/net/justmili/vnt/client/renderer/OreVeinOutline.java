package net.justmili.vnt.client.renderer;

import net.justmili.vnt.content.mechanics.logic.Veinminer;
import net.justmili.vnt.core.api.renderer.BlockGroupOutlineContext;
import net.justmili.vnt.core.api.renderer.BlockGroupOutlineRenderer;
import net.justmili.vnt.core.util.TagUtil;
import net.justmili.vnt.core.util.client.OutlineRGB;

public class OreVeinOutline {

    // Render white outline around the whole ore vein when the player looks at it with a pickaxe in hand
    public static boolean render(BlockGroupOutlineContext group) {
        var player = group.player();
        var level = group.level();
        if (player == null || level == null) return true;

        var state = level.getBlockState(group.blockPos());
        if (!Veinminer.canSeeVein(player, state)) return true;

        var color = OutlineRGB.get(Veinminer.requireShift(player));
        return BlockGroupOutlineRenderer.render(group, player, TagUtil.matchOreTags(state), Veinminer.RADIUS, Veinminer.SIZE,
            color.a(), color.r(), color.g(), color.b(), true);
    }
}