package com.kayandcarl;

import com.kayandcarl.registry.ModBlocks;
import com.kayandcarl.registry.ModEffects;
import com.kayandcarl.registry.ModEntityTypes;
import com.kayandcarl.registry.ModItemGroups;
import com.kayandcarl.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for Groomapack (Kay & Carl's Codex).
 *
 * Fabric calls {@link #onInitialize()} once, early in the game's startup,
 * on BOTH the client and the dedicated server. This is where we register
 * everything that must exist on both sides: items, blocks, entities,
 * status effects, recipes, etc.
 *
 * Anything that is purely visual (item models, entity renderers, GUI screens)
 * does NOT go here — it goes in {@link KayAndCarlClient}, which only runs on
 * the player's game, never on a headless server.
 */
public class KayAndCarl implements ModInitializer {

    /**
     * The mod id. This single string is used EVERYWHERE:
     *   - as the namespace for every item/block/entity ("kayandcarl:kays_24_inch")
     *   - as the folder name under assets/ and data/
     * Keep it lowercase, no spaces. Never change it once the world has saved
     * with it, or existing items in saved worlds will vanish.
     */
    public static final String MOD_ID = "kayandcarl";

    /**
     * A shared logger. Use LOGGER.info("...") to print messages to the game
     * console. Much better than System.out.println because it's tagged with
     * our mod name so we can find our messages in the log.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Helper to build an Identifier in our namespace.
     * Instead of writing Identifier.of("kayandcarl", "core_cell") everywhere,
     * we write KayAndCarl.id("core_cell"). Less typing, fewer typos.
     */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Groomapack (Kay & Carl's Codex)...");

        // ORDER MATTERS a little here. Effects and items are referenced by
        // recipes/blocks/entities, so we register the "leaf" content first,
        // then things that depend on it. Within Minecraft's registry system
        // this is mostly safe in any order, but this reads logically.
        ModEffects.registerEffects();      // Compression, Bleed, Confused
        ModItems.registerItems();          // Kay's 24 Inch, Core Dust, etc.
        ModBlocks.registerBlocks();        // Foundry Block, Rig Beacon
        ModEntityTypes.registerEntities(); // Tetoucher, Lurcher, Soot Hound...
        ModItemGroups.registerItemGroups();// our creative-tab so items are findable

        LOGGER.info("Groomapack initialized. Welcome to the basement.");
    }
}
