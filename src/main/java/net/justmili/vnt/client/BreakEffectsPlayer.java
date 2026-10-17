package net.justmili.vnt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.justmili.vnt.core.network.BreakEffectsPacket;
import net.justmili.vnt.core.util.Sounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class BreakEffectsPlayer {

    public static void play(ClientLevel level, BreakEffectsPacket packet) {
        if (level == null) return;

        var effects = packet.effectType();
        boolean particles = effects.particles();
        boolean sounds = effects.playAt(packet.index());
        var pos = packet.pos();
        var state = Block.stateById(packet.stateId());

        if (particles && sounds) {
            // LevelEvent constant name changed in 26.3 so it's just better to use the numerical value
            level.levelEvent(2001, pos, packet.stateId());
        } else if (sounds) {
            var sound = state.getSoundType();
            level.playLocalSound(pos, sound.getBreakSound(), SoundSource.BLOCKS, Sounds.randomVolume(sound), Sounds.randomPitch(sound), false);
        } else if (particles) {
            level.addDestroyBlockEffect(pos, state);
        }
    }
}