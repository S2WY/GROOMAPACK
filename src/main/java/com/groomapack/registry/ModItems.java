package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.item.Kays24InchItem;
import net.minecraft.item.Item;
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

    /** Core Grenade — thrown, shockwave knockback, zero block damage. */
    public static final Item CORE_GRENADE =
            register("core_grenade", new Item(new Item.Settings().maxCount(16)));

    /** Kay's Sharpening Stone — field repair for Kay's 24 Inch. */
    public static final Item SHARPENING_STONE =
            register("sharpening_stone", new Item(new Item.Settings().maxCount(16)));

    /** Ember Coating — applies temporary fire damage to a weapon. */
    public static final Item EMBER_COATING =
            register("ember_coating", new Item(new Item.Settings()));

    /** Void Tape — temporarily sticks a block to any surface. */
    public static final Item VOID_TAPE =
            register("void_tape", new Item(new Item.Settings()));

    /** Black Stamp — marks a mob to drop double loot. Broker-only. */
    public static final Item BLACK_STAMP =
            register("black_stamp", new Item(new Item.Settings().maxCount(16)));

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

    // ---- Carl Toolbox parts ---------------------------------------------

    public static final Item CARL_ARM =
            register("carl_arm", new Item(new Item.Settings().maxCount(16)));
    public static final Item CARL_LEG =
            register("carl_leg", new Item(new Item.Settings().maxCount(16)));
    public static final Item CARL_CORE =
            register("carl_core", new Item(new Item.Settings().maxCount(16)));

    /**
     * Does the actual registration into Minecraft's global ITEM registry.
     * @param name the path part of the id, e.g. "core_cell" -> "kayandcarl:core_cell"
     * @param item the item instance (with its Settings already configured)
     * @return the same item, so we can assign it to a field in one line
     */
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, KayAndCarl.id(name), item);
    }

    /**
     * Called from KayAndCarl.onInitialize(). The method body can be empty:
     * simply REFERENCING this class forces the JVM to run all the "static final"
     * field initializers above, which is what actually registers every item.
     * The log line gives us a clear "yes it ran" signal in the console.
     */
    public static void registerItems() {
        KayAndCarl.LOGGER.info("Registering Groomapack items");
    }
}
