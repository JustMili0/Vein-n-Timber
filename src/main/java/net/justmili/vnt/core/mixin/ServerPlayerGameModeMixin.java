package net.justmili.vnt.core.mixin;

import net.justmili.vnt.core.api.event.server.BlockBreakEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Unique
    private BlockState vnt$stateBefore;

    // Save the BlockState before the block is destroyed, since afterwards it's gone
    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void vnt$captureStateBefore(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.vnt$stateBefore = this.level.getBlockState(pos);
    }

    // Hook up own event for block breaking because Fabric API's event doesn't fully provide what I need
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void vnt$blockBrokenEvent(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakEvent.BLOCK_BROKEN.invoker().onBlockBroken(this.level, this.player, this.vnt$stateBefore, pos, cir.getReturnValueZ());
        this.vnt$stateBefore = null;
    }
}