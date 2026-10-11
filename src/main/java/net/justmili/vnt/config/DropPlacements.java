package net.justmili.vnt.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

// Enum prepared for later 1.1 update that will add a config
public enum DropPlacements {
    WHERE_BROKEN("default"),
    ORIGIN_POS("origin"),
    PLAYER_POS("player"),
    PLAYER_INV("inventory");

    DropPlacements(String entry) {
    }

    // Return position where block drop should be
    public BlockPos posByPlace(BlockPos broken, BlockPos origin, Player player) {
        return switch (this) {
            case WHERE_BROKEN -> broken;
            case ORIGIN_POS -> origin;
            case PLAYER_POS, PLAYER_INV -> player.blockPosition(); // PLAYER_INV uses this as the spot for what doesn't fit in the inventory
        };
    }
}