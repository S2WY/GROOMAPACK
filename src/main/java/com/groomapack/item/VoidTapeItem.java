package com.groomapack.item;

import com.groomapack.registry.ModBlocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

/**
 * Void Tape — a BlockItem wrapper that places {@link ModBlocks#VOID_TAPE_BLOCK}.
 *
 * Extending BlockItem means standard right-click-to-place behaviour is inherited
 * for free. VoidTapeBlock.onPlaced() handles the dissolve timer. This item is
 * consumed on placement (BlockItem handles stack decrement).
 */
public class VoidTapeItem extends BlockItem {

    public VoidTapeItem(Settings settings) {
        super(ModBlocks.VOID_TAPE_BLOCK, settings);
    }

    /** Play a slightly stickier sound on placement. */
    @Override
    public ActionResult place(ItemPlacementContext context) {
        ActionResult result = super.place(context);
        if (result.isAccepted() && !context.getWorld().isClient) {
            context.getWorld().playSound(null, context.getBlockPos(),
                    net.minecraft.sound.SoundEvents.BLOCK_SLIME_BLOCK_PLACE,
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        return result;
    }
}
