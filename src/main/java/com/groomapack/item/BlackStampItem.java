package com.groomapack.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Black Stamp — marks a mob to drop double loot on death.
 *
 * Right-click directly on a LivingEntity to apply the stamp. The entity
 * receives a "BlackStamped" flag in its NBT persist data. When it dies,
 * a ServerLivingEntityEvents.AFTER_DEATH listener (registered in KayAndCarl)
 * detects the flag and re-drops the entity's normal loot table a second time.
 *
 * The item is consumed on use. Stack 16.
 */
public class BlackStampItem extends Item {

    public static final String KEY_STAMPED = "BlackStamped";

    public BlackStampItem(Settings settings) {
        super(settings);
    }

    /**
     * useOnEntity is called when the player right-clicks directly on a living
     * entity. This is the cleanest hook for "apply effect to mob."
     */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user,
                                     LivingEntity entity, Hand hand) {
        if (entity instanceof PlayerEntity) return ActionResult.PASS; // can't stamp players

        if (!user.getWorld().isClient) {
            // Stamp the entity's custom persist data.
            NbtCompound data = entity.getCustomData();
            if (data.getBoolean(KEY_STAMPED)) {
                user.sendMessage(Text.literal("§7Already stamped."), true);
                return ActionResult.FAIL;
            }
            data.putBoolean(KEY_STAMPED, true);

            if (!user.getAbilities().creativeMode) stack.decrement(1);

            ((ServerWorld) user.getWorld()).spawnParticles(
                    ParticleTypes.CRIT,
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    8, 0.3, 0.3, 0.3, 0.1);
            user.getWorld().playSound(null, entity.getBlockPos(),
                    SoundEvents.ITEM_INK_SAC_USE, SoundCategory.PLAYERS, 1.0f, 0.8f);
            user.sendMessage(Text.literal("§7Stamped. It will drop double loot."), true);
        }

        return ActionResult.SUCCESS;
    }
}
