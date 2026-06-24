package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.block.FoundryBlock;
import com.groomapack.block.RigBeaconBlock;
import com.groomapack.block.VoidTapeBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

/**
 * Every block in Groomapack, with a corresponding BlockItem in the item registry.
 *
 * Pattern:
 *   1. Declare a {@code Block} field.
 *   2. {@code register()} puts the block in BLOCK registry AND creates a BlockItem
 *      in the ITEM registry under the same id.
 *   3. {@code registerBlocks()} is called in KayAndCarl.onInitialize() before
 *      ModItems (some items reference these blocks, e.g. VoidTapeItem).
 */
public class ModBlocks {

    /**
     * Void Tape Block — the temporary sticky block placed by VoidTapeItem.
     * Slime-like, full-cube, dissolves after 30 seconds via scheduled tick.
     */
    public static final Block VOID_TAPE_BLOCK = register("void_tape_block",
            new VoidTapeBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.PALE_GREEN)
                    .sounds(BlockSoundGroup.SLIME)
                    .strength(0.2f)
                    .slipperiness(0.98f)));   // slippery like slime

    /**
     * Foundry Block — right-click to instantly convert Raw Core Dust → Core Cell.
     * An industrial furnace that runs on Core Cell energy.
     */
    public static final Block FOUNDRY_BLOCK = register("foundry_block",
            new FoundryBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .sounds(BlockSoundGroup.METAL)
                    .strength(4.0f, 6.0f)
                    .requiresTool()
                    .luminance(state -> 7)));  // glows slightly when idle

    /**
     * Rig Beacon — right-click with Carl's Core to assemble The Rig above it.
     */
    public static final Block RIG_BEACON = register("rig_beacon",
            new RigBeaconBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.CYAN)
                    .sounds(BlockSoundGroup.METAL)
                    .strength(5.0f, 1200.0f)  // very blast-resistant, like a beacon
                    .requiresTool()
                    .luminance(state -> 15)));

    /** Registers the Block and its default BlockItem together. */
    private static Block register(String name, Block block) {
        Registry.register(Registries.BLOCK, KayAndCarl.id(name), block);
        Registry.register(Registries.ITEM, KayAndCarl.id(name),
                new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void registerBlocks() {
        KayAndCarl.LOGGER.info("Registering Groomapack blocks (VoidTapeBlock, FoundryBlock, RigBeacon)");
    }
}
