package com.groomapack.registry;

import com.groomapack.KayAndCarl;

/**
 * Central place where every BLOCK in Groomapack is registered:
 *   - The Foundry Block (custom crafting station with upgrade slots)
 *   - The Rig Beacon (mech charging / repair station)
 *
 * STUB FOR NOW. Blocks are added in the dedicated "blocks" step, because each
 * of these two has a BlockEntity + custom GUI attached, which is a chunk of
 * code I want to build and explain on its own rather than rush here.
 */
public class ModBlocks {

    public static void registerBlocks() {
        KayAndCarl.LOGGER.info("Registering Groomapack blocks (none yet — coming in the blocks step)");
    }
}
