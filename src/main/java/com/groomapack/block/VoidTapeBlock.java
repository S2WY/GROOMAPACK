package com.groomapack.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Void Tape Block — the placed form of Void Tape.
 *
 * Looks like a sticky translucent slab. Immediately after placement a 30-second
 * scheduled tick is set; when it fires the block silently dissolves. In that
 * window it acts as a normal solid block — climbable, walkable, sealable.
 *
 * Use-cases:
 *   • Seal a door from the Tetoucher for 30 seconds (buys time)
 *   • Temporary bridge / step
 *   • Trap: place on a 2-wide gap, enemy falls through when it dissolves
 */
public class VoidTapeBlock extends Block {

    private static final int DISSOLVE_TICKS = 600; // 30 seconds at 20 TPS

    public VoidTapeBlock(Settings settings) {
        super(settings);
    }

    /**
     * Called by the Block placement code immediately after the block enters the
     * world. We schedule the dissolve tick here so every placed copy has a
     * guaranteed single-fire timer.
     */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                          LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, DISSOLVE_TICKS);
        }
    }

    /**
     * Fires once, exactly DISSOLVE_TICKS after placement. The block removes
     * itself — no drops, no ItemScatter, just a soft dissolve sound.
     */
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.removeBlock(pos, false);
        world.playSound(null, pos,
                SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.6f, 1.5f);
    }

    /** Can be pushed by pistons (intentional — players can extend the bridge). */
    @Override
    public net.minecraft.block.piston.PistonBehavior getPistonBehavior(BlockState state) {
        return net.minecraft.block.piston.PistonBehavior.NORMAL;
    }
}
