package com.groomapack.block;

import com.groomapack.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Foundry Block — Kay's basement smelting station.
 *
 * Lore: a repurposed industrial furnace that runs on Core Cell energy. Kay
 * used it to synthesize the original 24 Inch. It hums and pops with barely
 * contained heat.
 *
 * Mechanic (right-click):
 *   Instantly converts ALL Raw Core Dust in the player's inventory to Core
 *   Cells (1:1 ratio), bypassing the smelting wait. This represents the
 *   Foundry's superior throughput compared to a vanilla blast furnace.
 *
 *   If the player has nothing to smelt, shows a message and plays a click.
 *
 * Future: dedicated GUI screen with a 4-slot input, a fuel slot (Core Cell),
 * and unique Foundry-exclusive recipes (Carl Toolbox assembly). Replacing this
 * right-click logic with a proper ScreenHandler is a polish step.
 */
public class FoundryBlock extends Block {

    public FoundryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        int converted = 0;
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slot = inventory.getStack(i);
            if (slot.isOf(ModItems.RAW_CORE_DUST)) {
                int count = slot.getCount();
                converted += count;
                slot.setCount(0);
                // Add Core Cells directly; if inventory is full, drop them.
                ItemStack cells = new ItemStack(ModItems.CORE_CELL, count);
                if (!inventory.insertStack(cells)) {
                    player.dropItem(cells, false);
                }
            }
        }

        if (converted > 0) {
            player.sendMessage(
                    Text.literal("§6[Foundry] §fConverted §e" + converted +
                                 "x Raw Core Dust §fto §eCore Cells§f."), false);
            world.playSound(null, pos,
                    SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5,
                        pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.3, 0.1, 0.3, 0.05);
            }
        } else {
            player.sendMessage(
                    Text.literal("§7[Foundry] No Raw Core Dust to smelt."), true);
            world.playSound(null, pos,
                    SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.6f, 0.8f);
        }

        return ActionResult.SUCCESS;
    }
}
