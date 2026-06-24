package com.groomapack.registry;

import com.groomapack.KayAndCarl;

/**
 * Central place where every custom STATUS EFFECT is registered:
 *   - Compression (stacks to 3, then a knockback "pop" — from The Lungbreaker)
 *   - Bleed (2 damage/sec for 6s, stackable — from the Serrated Fang)
 *   - Confused (the Tetoucher's 3-second freeze when you remove your boots)
 *
 * STUB FOR NOW. We register these in the "status effects" step, right before
 * the weapons/mobs that apply them, so the behavior and the thing that uses it
 * are explained together.
 */
public class ModEffects {

    public static void registerEffects() {
        KayAndCarl.LOGGER.info("Registering Groomapack status effects (none yet — coming in the effects step)");
    }
}
