package net.justmili.vnt.core.util.client;

import net.justmili.vnt.config.Config;
import net.justmili.vnt.config.FloatColor;

public class OutlineRGB {

    public static FloatColor get(boolean active) {
        return active? Config.activeOutlineColor : Config.inactiveOutlineColor;
    }
}