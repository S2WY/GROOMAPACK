package com.groomapack.item;

import com.groomapack.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Kay's Sharpening Stone — field repair for Kay's 24 Inch.
 *
 * Hold the stone in one hand and Kay's 24 Inch in the other, then right-click.
 * Restores 200 durability per stone. The item is consumed on use.
 *
 * Why 200? The 24 Inch has 2500 max durability. One stone covers roughly 8%
 * of the bar — meaningful but not trivially stackable to full.
 *
 * Crafted: 2 Flint + 1 Stick (produces 3 stones).
 */
public class SharpeningStoneItem extends Item {

    private static final int REPAIR_AMOUNT = 200;

    public SharpeningStoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stone = user.getStackInHand(hand);
        Hand otherHand = (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack target = user.getStackInHand(otherHand);

        if (target.isOf(ModItems.KAYS_24_INCH) && target.getDamage() > 0) {
            if (!world.isClient) {
                int newDamage = Math.max(0, target.getDamage() - REPAIR_AMOUNT);
                target.setDamage(newDamage);

                if (!user.getAbilities().creativeMode) stone.decrement(1);

                user.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 0.8f, 1.4f);
                user.sendMessage(
                        Text.literal("§aKay's 24 Inch sharpened. §7(-" + REPAIR_AMOUNT + " damage)"),
                        true);
            }
            return TypedActionResult.success(stone, world.isClient);
        }

        if (!world.isClient) {
            user.sendMessage(Text.literal("§7Nothing to sharpen here."), true);
        }
        return TypedActionResult.pass(stone);
    }
}
