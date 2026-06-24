package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.item.BlackStampItem;
import com.groomapack.item.CoreGrenadeItem;
import com.groomapack.item.EmberCoatingItem;
import com.groomapack.item.Kays24InchItem;
import com.groomapack.item.SharpeningStoneItem;
import com.groomapack.item.VoidTapeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Central place where every ITEM in Groomapack is created and registered.
 *
 * Pattern used here:
 *   1. Declare a "public static final Item NAME;" field for each item so the
 *      rest of the code can reference it (e.g. recipes, creative tab, drops).
 *   2. A private register(...) helper does the actual Registry.register call.
 *   3. registerItems() is called once from KayAndCarl.onInitialize().
 *
 * Why a helper method? Minecraft requires every item to be put into the global
 * ITEM registry under a unique Identifier. Doing that by hand for 30+ items is
 * repetitive and error-prone, so we wrap it once.
 *
 * NOTE: Right now the "special" items (Kay's 24 Inch, etc.) are plain Items so
 * the project compiles and the items exist in-game. In later steps we'll swap
 * them for dedicated classes (e.g. Kays24InchItem) that hold the real behavior
 * (multi-tool, reach, modes). The registry line stays almost identical.
 */
public class ModItems {

    // ---- The Codex's signature gear -------------------------------------

    /**
     * Kay's 24 Inch — the universal multi-tool.
     * maxCount(1): only one can be held at a time (it's a unique weapon, not stackable).
     * maxDamage(2500): more durable than netherite (2031), earned through Kay's Basement.
     */
    public static final Item KAYS_24_INCH =
            register("kays_24_inch", new Kays24InchItem(new Item.Settings().maxCount(1).maxDamage(2500)));

    /** Raw Core Dust — extracted from dirt by Kay's 24 Inch, smelted into Core Cells. */
    public static final Item RAW_CORE_DUST =
            register("raw_core_dust", new Item(new Item.Settings()));

    /** Core Cell — primary fuel for The Rig, tames Soot Hounds, crafting ingredient. */
    public static final Item CORE_CELL =
            register("core_cell", new Item(new Item.Settings()));

    // ---- Consumables & utility items ------------------------------------

    /** Core Grenade — thrown, shockwave knockback 5-block radius, zero block damage. */
    public static final Item CORE_GRENADE =
            register("core_grenade", new CoreGrenadeItem(new Item.Settings().maxCount(16)));

    /** Kay's Sharpening Stone — right-click to field-repair Kay's 24 Inch (−200 durability). */
    public static final Item SHARPENING_STONE =
            register("sharpening_stone", new SharpeningStoneItem(new Item.Settings().maxCount(16)));

    /** Ember Coating — right-click with Kay's 24 Inch in other hand → 5-min fire on every hit. */
    public static final Item EMBER_COATING =
            register("ember_coating", new EmberCoatingItem(new Item.Settings().maxCount(1)));

    /** Void Tape — places a temporary block that dissolves after 30 seconds. */
    public static final Item VOID_TAPE =
            register("void_tape", new VoidTapeItem(new Item.Settings().maxCount(16)));

    /** Black Stamp — right-click a mob to mark it for double loot on death. */
    public static final Item BLACK_STAMP =
            register("black_stamp", new BlackStampItem(new Item.Settings().maxCount(16)));

    // ---- Mob drops & crafting materials ---------------------------------

    /** Ember Dust — Soot Hound drop. */
    public static final Item EMBER_DUST =
            register("ember_dust", new Item(new Item.Settings()));

    /** Phantom Grit — Gravel Wraith drop. */
    public static final Item PHANTOM_GRIT =
            register("phantom_grit", new Item(new Item.Settings()));

    /** Tetoucher Leather — Tetoucher drop, used for Grip Boots & Stealth Plating. */
    public static final Item TETOUCHER_LEATHER =
            register("tetoucher_leather", new Item(new Item.Settings()));

    /** Tetoucher Bone — Tetoucher drop, used for the Nullrod. */
    public static final Item TETOUCHER_BONE =
            register("tetoucher_bone", new Item(new Item.Settings()));

    /** Shoearm — rare (20%) Tetoucher drop, required for Carl's Arm. */
    public static final Item SHOEARM =
            register("shoearm", new Item(new Item.Settings()));

    // ---- Spawn eggs -------------------------------------------------------
    // Registered in registerItems() because EntityTypes must exist first.
    // Eggs for Lurcher/GravelWraith/SootHound/Broker are added in M3 when
    // those entity classes are implemented.
    public static Item TETOUCHER_SPAWN_EGG;
    public static Item LURCHER_SPAWN_EGG;
    public static Item GRAVEL_WRAITH_SPAWN_EGG;
    public static Item SOOT_HOUND_SPAWN_EGG;
    public static Item BROKER_SPAWN_EGG;
    public static Item THE_RIG_SPAWN_EGG;

    // ---- Carl Toolbox parts ---------------------------------------------

    public static final Item CARL_ARM =
            register("carl_arm", new Item(new Item.Settings().maxCount(16)));
    public static final Item CARL_LEG =
            register("carl_leg", new Item(new Item.Settings().maxCount(16)));
    public static final Item CARL_CORE =
            register("carl_core", new Item(new Item.Settings().maxCount(16)));

    /**
     * Does the actual registration into Minecraft's global ITEM registry.
     * @param name the path part of the id, e.g. "core_cell" -> "groomapack:core_cell"
     * @param item the item instance (with its Settings already configured)
     * @return the same item, so we can assign it to a field in one line
     */
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, KayAndCarl.id(name), item);
    }

    /**
     * Called from KayAndCarl.onInitialize(). Accessing any static final field
     * above is enough to trigger the class initializer (which does all the
     * Registry.register calls). The log line confirms the block ran.
     *
     * Spawn eggs cannot be declared as static-final because they need the
     * EntityType references to already be registered first.
     */
    public static void registerItems() {
        KayAndCarl.LOGGER.info("Registering Groomapack items");
        TETOUCHER_SPAWN_EGG = register("tetoucher_spawn_egg",
                new SpawnEggItem(ModEntityTypes.TETOUCHER, 0x1A1A2E, 0xBF0000, new Item.Settings()));
        // The remaining 5 eggs are registered in registerMobSpawnEggs(), called
        // from ModEntityTypes.registerEntities() after all entity types exist.
    }

    /** Called from ModEntityTypes after all mob entity types exist. */
    public static void registerMobSpawnEggs() {
        LURCHER_SPAWN_EGG = register("lurcher_spawn_egg",
                new SpawnEggItem(ModEntityTypes.LURCHER,     0x4A3000, 0xCCCC00, new Item.Settings()));
        GRAVEL_WRAITH_SPAWN_EGG = register("gravel_wraith_spawn_egg",
                new SpawnEggItem(ModEntityTypes.GRAVEL_WRAITH, 0x888888, 0xDDDDDD, new Item.Settings()));
        SOOT_HOUND_SPAWN_EGG = register("soot_hound_spawn_egg",
                new SpawnEggItem(ModEntityTypes.SOOT_HOUND,  0x222222, 0xFF6600, new Item.Settings()));
        BROKER_SPAWN_EGG = register("broker_spawn_egg",
                new SpawnEggItem(ModEntityTypes.BROKER,      0x2C2C5E, 0xE8D5B7, new Item.Settings()));
        THE_RIG_SPAWN_EGG = register("the_rig_spawn_egg",
                new SpawnEggItem(ModEntityTypes.THE_RIG,     0x808080, 0xFF4400, new Item.Settings()));
    }
}
