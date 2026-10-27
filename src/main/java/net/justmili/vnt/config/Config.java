package net.justmili.vnt.config;

// To be finalized and fully implemented in 1.1 with Millie's Core Libraries
public class Config {
    /* To additionally add in 1.1:
    - List of blocks/tags that can be veinmined
    - List of blocks/tags that can be chopped down
    - List of blocks/tags considered as leaves
    - List of veinmine tools (item/tag)
    - List of timber tools (item/tag)
    */
    public static boolean shiftToVeinmine;
    public static boolean shiftToTimber;
    public static int maxVeinSize;
    public static int maxTrunkSize;
    public static int maxLeavesSize;
    public static boolean breakTreeLeaves;
    public static boolean leavesDamageTool;
    public static DropPlacements veinDropPlace;
    public static DropPlacements trunkDropPlace;
    public static DropPlacements leavesDropPlace;
    public static boolean includeLeavesDrops;
    public static boolean replantTrees;

    public static boolean veinBreakParticles;
    public static boolean trunkBreakParticles;
    public static boolean leavesBreakParticles;
    public static float veinBreakSoundPercentage;
    public static float trunkBreakSoundsPercentage;
    public static float leavesBreakSoundPercentage;

    public static FloatColor inactiveOutlineColor;
    public static FloatColor activeOutlineColor;

    public static void initCommon() { // Create as common-server-priority
        shiftToVeinmine = true;
        shiftToTimber = false;
        maxVeinSize = 64;
        maxTrunkSize = 256;
        maxLeavesSize = 256;
        breakTreeLeaves = true;
        leavesDamageTool = false;
        veinDropPlace = DropPlacements.ORIGIN_POS;
        trunkDropPlace = DropPlacements.WHERE_BROKEN;
        leavesDropPlace = DropPlacements.WHERE_BROKEN;
        includeLeavesDrops = false;
        replantTrees = true;

        // Somewhat shared by server and client
        // If client doesn't have the mod installed, mod uses server defaults to play sounds
        // Otherwise, it plays from client's config
        veinBreakParticles = true;
        trunkBreakParticles = false;
        leavesBreakParticles = true;
        veinBreakSoundPercentage = 1f;
        trunkBreakSoundsPercentage = 0.8f;
        leavesBreakSoundPercentage = 0.02f;
    }

    public static void initClient() {
        inactiveOutlineColor = new FloatColor(0.6f, 0.4f);
        activeOutlineColor = new FloatColor(0.6f, 1f);
    }
}