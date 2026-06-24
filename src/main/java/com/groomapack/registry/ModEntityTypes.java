package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.entity.BrokerEntity;
import com.groomapack.entity.GravelWraithEntity;
import com.groomapack.entity.LurcherEntity;
import com.groomapack.entity.SootHoundEntity;
import com.groomapack.entity.TetoucherEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

/**
 * Registers every mob in Groomapack and wires up:
 *   1. EntityType (factory + hitbox)
 *   2. Default attributes (health, speed, damage)
 *   3. Natural spawn rules (where/when they can spawn)
 *   4. Biome spawn weights (how often they appear)
 */
public class ModEntityTypes {

    // ------------------------------------------------------------------ mobs

    /**
     * Tetoucher — tall lurking horror, wall-climbs, teleports, confused by
     * barefoot players. 0.7 wide × 2.9 tall (Enderman-height silhouette).
     */
    public static final EntityType<TetoucherEntity> TETOUCHER = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("tetoucher"),
            EntityType.Builder.create(TetoucherEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 2.9f)
                    .maxTrackingRange(10)
                    .build("tetoucher"));

    /**
     * Lurcher — fast, weak swarmer. Sprints in packs of 2-4. Easy solo, deadly
     * in numbers. 0.6 wide × 1.8 tall (human-ish but hunched).
     */
    public static final EntityType<LurcherEntity> LURCHER = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("lurcher"),
            EntityType.Builder.create(LurcherEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(8)
                    .build("lurcher"));

    /**
     * Gravel Wraith — drifting hostile, immune to projectiles, drops Phantom
     * Grit. 1.0 wide × 0.5 tall (flat, flickering silhouette).
     */
    public static final EntityType<GravelWraithEntity> GRAVEL_WRAITH = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("gravel_wraith"),
            EntityType.Builder.create(GravelWraithEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.0f, 0.5f)
                    .maxTrackingRange(8)
                    .build("gravel_wraith"));

    /**
     * Soot Hound — tameable canine mob. Drops Ember Dust. Tame with a Core Cell.
     * Untamed: skittish and aggressive if cornered. Tamed: guards its owner.
     * 0.6 wide × 0.85 tall (wolf-scale).
     */
    public static final EntityType<SootHoundEntity> SOOT_HOUND = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("soot_hound"),
            EntityType.Builder.create(SootHoundEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 0.85f)
                    .maxTrackingRange(8)
                    .build("soot_hound"));

    /**
     * The Broker — neutral trader found in Kay's Basement and rarely in the
     * overworld. Attacks if hit. Trades rare items for Black Stamps.
     * 0.6 wide × 1.95 tall (slightly taller than a villager).
     */
    public static final EntityType<BrokerEntity> BROKER = Registry.register(
            Registries.ENTITY_TYPE,
            KayAndCarl.id("broker"),
            EntityType.Builder.create(BrokerEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .maxTrackingRange(10)
                    .build("broker"));

    // ------------------------------------------------------------------ init

    public static void registerEntities() {
        KayAndCarl.LOGGER.info("Registering Groomapack entities");

        // Attributes — must be registered before any instance spawns.
        FabricDefaultAttributeRegistry.register(TETOUCHER,    TetoucherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(LURCHER,      LurcherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GRAVEL_WRAITH,GravelWraithEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOOT_HOUND,   SootHoundEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BROKER,       BrokerEntity.createAttributes());

        registerSpawnRules();

        // Spawn eggs for M3 mobs — called here because we need all EntityTypes first.
        ModItems.registerMobSpawnEggs();
    }

    /**
     * SpawnRestriction tells the game WHEN a mob is allowed to occupy a block
     * (light level, surface type). BiomeModifications tells it WHERE (which
     * biomes) and HOW OFTEN (weight, group size).
     *
     * Tetoucher:    rare, solo, dark underground/overworld
     * Lurcher:      common, packs, any dark area
     * GravelWraith: uncommon, solo/pair, underground (caves, ravines)
     * SootHound:    creature group, surface/forest biomes, dusk-only
     * Broker:       very rare, never spawns naturally (only Kay's Basement spawn)
     */
    private static void registerSpawnRules() {
        // Tetoucher — surface or cave, dark (light ≤ 7), rare
        SpawnRestriction.register(TETOUCHER,
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                HostileEntity::canSpawnInDark);
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.MONSTER, TETOUCHER,
                8, 1, 2);   // weight 8, group 1-2

        // Lurcher — common swarmer in the dark
        SpawnRestriction.register(LURCHER,
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                HostileEntity::canSpawnInDark);
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.MONSTER, LURCHER,
                25, 2, 4);  // weight 25, packs of 2-4

        // Gravel Wraith — underground only
        SpawnRestriction.register(GRAVEL_WRAITH,
                SpawnRestriction.Location.IN_WATER,   // floats; no ground check
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                HostileEntity::canSpawnInDark);
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.MONSTER, GRAVEL_WRAITH,
                12, 1, 2);

        // Soot Hound — creature (spawns like animals), surface
        SpawnRestriction.register(SOOT_HOUND,
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getLightLevel(pos) > 7);
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(net.minecraft.registry.tag.BiomeTags.IS_OVERWORLD),
                SpawnGroup.CREATURE, SOOT_HOUND,
                6, 1, 3);

        // Broker — no natural spawning (Kay's Basement only)
        // SpawnRestriction not registered; handled by structure spawner.
    }
}
