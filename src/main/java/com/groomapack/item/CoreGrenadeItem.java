package com.groomapack.item;

import com.groomapack.entity.CoreGrenadeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Core Grenade — right-click to hurl; shockwave knocks back all entities in
 * 5 blocks. No damage, no block destruction. Pure crowd-control / escape tool.
 *
 * Stack size 16. Crafted: Core Cell + 4 Iron Nuggets (cross pattern).
 */
public class CoreGrenadeItem extends Item {

    public CoreGrenadeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.playSound(SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.8f);

        if (!world.isClient) {
            CoreGrenadeEntity grenade = new CoreGrenadeEntity(world, user);
            grenade.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
            world.spawnEntity(grenade);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return ActionResult.SUCCESS;
    }
}
