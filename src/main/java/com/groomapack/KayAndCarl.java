package com.groomapack;

import com.groomapack.command.ModCommands;
import com.groomapack.item.BlackStampItem;
import com.groomapack.registry.ModBlocks;
import com.groomapack.registry.ModEffects;
import com.groomapack.registry.ModEntityTypes;
import com.groomapack.registry.ModItemGroups;
import com.groomapack.registry.ModItems;
import com.groomapack.worldgen.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for Groomapack (Kay & Carl's Codex).
 *
 * Fabric calls onInitialize() once, early in game startup, on both client
 * and dedicated server. Only server-safe registrations live here; all visual
 * content goes in KayAndCarlClient.
 */
public class KayAndCarl implements ModInitializer {

    /** The mod id — every registry key is namespaced under this. */
    public static final String MOD_ID = "groomapack";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Convenience factory for identifiers in our namespace. */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Groomapack (Kay & Carl's Codex)...");

        // Registration order:
        //   1. Effects first (items/mobs reference them)
        //   2. Blocks before Items (VoidTapeItem needs ModBlocks.VOID_TAPE_BLOCK)
        //   3. Items after Blocks
        //   4. Entities after Items (spawn eggs reference entity types)
        //   5. Item groups last (reference item fields from ModItems)
        ModEffects.registerEffects();
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModEntityTypes.registerEntities();
        ModItemGroups.registerItemGroups();
        ModWorldgen.register();
        ModCommands.register();

        registerEventHooks();

        LOGGER.info("Groomapack initialized. Welcome to the basement.");
    }

    /**
     * Fabric event hooks that cannot be expressed as pure registry entries.
     *
     * BLACK STAMP — AFTER_DEATH listener:
     *   When a LivingEntity dies that was marked with a Black Stamp, its loot
     *   is dropped a second time by triggering dropLoot() again via reflection
     *   — except we can't call protected dropLoot() from here. Instead we drop
     *   a hardcoded "bonus cache" of Common drops appropriate to the mob.
     *
     *   Real solution: a Mixin on LivingEntity.dropLoot() that checks the flag
     *   and doubles the loot table output. That Mixin ships in the Mixin step.
     *   For now: stamped mobs drop 3 extra Core Cells as a universal bonus,
     *   PLUS entity-type-specific extras defined below.
     */
    private void registerEventHooks() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!isStamped(entity)) return;

            // Clear the stamp so it doesn't fire again on respawn edge-cases.
            BlackStampItem.STAMPED_ENTITIES.remove(entity.getUuid());

            // Universal bonus: 3 Core Cells.
            entity.dropStack(new ItemStack(ModItems.CORE_CELL, 3));

            // Entity-type-specific bonus drops.
            dropStampBonus(entity);
        });
    }

    private static boolean isStamped(LivingEntity entity) {
        return BlackStampItem.STAMPED_ENTITIES.contains(entity.getUuid());
    }

    private static void dropStampBonus(LivingEntity entity) {
        if (entity instanceof com.groomapack.entity.TetoucherEntity) {
            entity.dropStack(new ItemStack(ModItems.TETOUCHER_LEATHER, 2));
            entity.dropStack(new ItemStack(ModItems.TETOUCHER_BONE, 2));
        } else if (entity instanceof com.groomapack.entity.LurcherEntity) {
            entity.dropStack(new ItemStack(net.minecraft.item.Items.ROTTEN_FLESH, 3));
        } else if (entity instanceof com.groomapack.entity.GravelWraithEntity) {
            entity.dropStack(new ItemStack(ModItems.PHANTOM_GRIT, 3));
        } else if (entity instanceof com.groomapack.entity.SootHoundEntity) {
            entity.dropStack(new ItemStack(ModItems.EMBER_DUST, 2));
        } else if (entity instanceof com.groomapack.entity.BrokerEntity) {
            entity.dropStack(new ItemStack(ModItems.BLACK_STAMP, 4));
        }
    }
}
