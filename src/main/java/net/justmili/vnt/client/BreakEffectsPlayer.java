package net.justmili.vnt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.justmili.vnt.core.network.BreakEffectsPacket;
import net.justmili.vnt.core.util.Sounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class BreakEffectsPlayer {

    public static void play(ClientLevel level, BreakEffectsPacket packet) {
        if (level == null) return;

        var effects = packet.effectType();
        boolean particles = effects.particles();
        if (!particles && effects.soundPercentage() <= 0) return;

        var positions = packet.positions();
        var stateIds = packet.stateIds();
        for (int index = 0; index < positions.length; index++) {
            boolean sounds = effects.playAt(index);
            if (!particles && !sounds) continue;

            var pos = BlockPos.of(positions[index]);
            var state = Block.stateById(stateIds[index]);

            if (particles && sounds) {
                // LevelEvent constant name changed in 26.3 so it's just better to use the numerical value
                level.levelEvent(2001, pos, stateIds[index]);
            } else if (sounds) {
                var sound = state.getSoundType();
                level.playLocalSound(pos, sound.getBreakSound(), SoundSource.BLOCKS, Sounds.randomVolume(sound), Sounds.randomPitch(sound), false);
            } else {
                level.addDestroyBlockEffect(pos, state);
            }
        }
    }
}