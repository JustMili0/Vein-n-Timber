package net.justmili.vnt.core.util;

import net.minecraft.world.level.block.SoundType;

// Stripped down sound util from Millie's Core Libraries. Temporary until I switch this to actually make this use the library
public class Sounds {

    public static float randomPitch(SoundType sound) {
        return sound.getPitch() * 0.8f;
    }

    public static float randomVolume(SoundType sound) {
        return (sound.getVolume() + 1f) / 2f;
    }
}