package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.entity.TetoucherEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Central place where every ENTITY (mob + the Rig mech) is registered:
 *   Tetoucher, Lurcher, Gravel Wraith, The Broker, Soot Hound, and The Rig.
 *
 * Only the TETOUCHER exists so far. Each entity needs four things wired up:
 *   1. An Entity class with its AI/behaviour      → {@link TetoucherEntity}
 *   2. An EntityType registration (here)          → gives it an id + dimensions
 *   3. Default attributes (here)                  → health, speed, damage
 *   4. A client-side renderer + model             → registered in KayAndCarlClient
 */
public class ModEntityTypes {

    /**
     * The Tetoucher entity type.
     *
     *   create(TetoucherEntity::new, SpawnGroup.MONSTER) — factory + which spawn
     *       category it belongs to (affects mob caps and spawn rules).
     *   dimensions(0.7f, 2.9f) — hitbox width × height. Tall and thin: it towers
     *       over the player (a bit shorter than the 2.9-block Enderman is tall...
     *       actually matched here for that unsettling looming silhouette).
     *   maxTrackingRange(10) — how far away (in chunks) clients are told it exists.
     *
     *   build("tetoucher") — the string id is used by the game's datafixer system.
     */
    public static final EntityType<TetoucherEntity> TETOUCHER = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("tetoucher"),
            EntityType.Builder.create(TetoucherEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 2.9f)
                    .maxTrackingRange(10)
                    .build("tetoucher"));

    /**
     * Called from KayAndCarl.onInitialize(). As well as forcing the static field
     * above to register, we register the Tetoucher's default attributes here.
     * FabricDefaultAttributeRegistry MUST run during common init — without it the
     * game crashes the instant a Tetoucher tries to spawn ("no attributes").
     */
    public static void registerEntities() {
        KayAndCarl.LOGGER.info("Registering Groomapack entities (Tetoucher)");
        FabricDefaultAttributeRegistry.register(TETOUCHER, TetoucherEntity.createAttributes());
    }
}
