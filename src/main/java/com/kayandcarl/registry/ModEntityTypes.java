package com.kayandcarl.registry;

import com.kayandcarl.KayAndCarl;

/**
 * Central place where every ENTITY (mob + the Rig mech) is registered:
 *   Tetoucher, Lurcher, Gravel Wraith, The Broker, Soot Hound, and The Rig.
 *
 * STUB FOR NOW. Entities are the heaviest feature in the mod — each needs an
 * Entity class (AI/behavior), an EntityType registration, default attributes
 * (health, speed, damage), and a client-side renderer + model. We build the
 * Tetoucher first, in its own step, then the rest.
 */
public class ModEntityTypes {

    public static void registerEntities() {
        KayAndCarl.LOGGER.info("Registering Groomapack entities (none yet — coming in the mobs step)");
    }
}
