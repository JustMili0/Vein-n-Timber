package net.justmili.vnt.core.util;

import net.minecraft.util.Mth;

// Stripped down math util from Millie's Core Libraries. Temporary until I switch this to actually make this use the library
public class Maths {

    public static float clamp(float value, float min, float max) {
        return value < min? min : Math.min(value, max);
    }

    public static int ceil(float value) {
        return Mth.ceil(value);
    }

    public static long floorDiv(long dividend, long divisor) {
        return Math.floorDiv(dividend, divisor);
    }
}