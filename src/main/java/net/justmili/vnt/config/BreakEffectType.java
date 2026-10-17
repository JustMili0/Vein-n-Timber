package net.justmili.vnt.config;

import net.justmili.vnt.core.util.Maths;

// Enum prepared for later 1.1 update that will add a config
public enum BreakEffectType {
    VEIN,
    TRUNK,
    LEAVES;

    public boolean particles() {
        return switch (this) {
            case VEIN -> Config.veinBreakParticles;
            case TRUNK -> Config.trunkBreakParticles;
            case LEAVES -> Config.leavesBreakParticles;
        };
    }

    public float soundPercentage() {
        return switch (this) {
            case VEIN -> Config.veinBreakSoundPercentage;
            case TRUNK -> Config.trunkBreakSoundsPercentage;
            case LEAVES -> Config.leavesBreakSoundPercentage;
        };
    }

    // Return whether the block at this index plays its break sound, spreading the sounds evenly over the group
    public boolean playAt(int index) {
        float soundPercentage = soundPercentage();
        return soundPercentage > 0 && Maths.ceil((index + 1) * soundPercentage) > Maths.ceil(index * soundPercentage);
    }
}