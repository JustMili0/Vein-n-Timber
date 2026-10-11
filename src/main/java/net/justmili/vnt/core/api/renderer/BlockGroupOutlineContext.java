package net.justmili.vnt.core.api.renderer;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

//? if < 1.21.11 {
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
//?} else if < 26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
*///?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
*///?}
//? if >= 26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
*///?} else {
import com.mojang.blaze3d.vertex.VertexConsumer;
//?}
//? if < 1.21.11 {
import net.minecraft.client.renderer.RenderType;
//?} else {
/*import net.minecraft.client.renderer.rendertype.RenderTypes;
 *///?}

/**
 * Idk something to lesser the stonecutter template mess in BlockGroupOutlineRenderer
 */
public record BlockGroupOutlineContext(/*? if >= 26.1 {*//*LevelRenderContext*//*?} else {*/WorldRenderContext/*?}*/ context, /*? if >= 1.21.11 {*//*BlockOutlineRenderState*//*?} else {*/WorldRenderContext.BlockOutlineContext/*?}*/ outline) {
    private static final Minecraft client = Minecraft.getInstance();

    public Window window() {
        return client.getWindow();
    }

    public Player player() {
        return client.player;
    }

    public ClientLevel level() {
        return client.level;
    }

    public PoseStack poseStack() {
        return context./*? if >=26.1 {*//*poseStack()*//*?} else if >=1.21.11 {*//*matrices()*//*?} else {*/matrixStack()/*?}*/;
    }

    public Vec3 cameraPos() {
        return /*? if >=26.1 {*//*context.levelState().cameraRenderState.pos*//*?} else if >=1.21.11 {*//*context.worldState().cameraRenderState.pos*//*?} else {*/new Vec3(outline.cameraX(), outline.cameraY(), outline.cameraZ())/*?}*/;
    }

    public BlockPos blockPos() {
        return outline./*? if >=1.21.11 {*//*pos()*//*?} else {*/blockPos()/*?}*/;
    }

    //? if >=26.2 {
    /*public SubmitNodeCollector collector() {
        return context.submitNodeCollector();
    }

    public RenderType lines() {
        return RenderTypes.lines();
    }
    *///?} else {
    public VertexConsumer lines() {
        var source = context./*? if >=26.1 {*//*bufferSource()*//*?} else {*/consumers()/*?}*/;
        return /*? if <1.21.11 {*/source == null? null : /*?}*/source.getBuffer(/*? if >=1.21.11 {*//*RenderTypes*//*?} else {*/RenderType/*?}*/.lines());
    }
    //?}
}