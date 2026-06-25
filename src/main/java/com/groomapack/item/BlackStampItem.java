package com.groomapack.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Black Stamp — marks a mob to drop double loot on death.
 *
 * Right-click directly on a LivingEntity to apply the stamp. The entity's UUID
 * is added to STAMPED_ENTITIES. When it dies, the AFTER_DEATH listener in
 * KayAndCarl detects the UUID and drops a bonus loot cache.
 *
 * Note: the stamp is in-memory only and does not survive server restarts.
 * A persistent NBT/attachment solution is planned for a later step.
 */
public class BlackStampItem extends Item {

    public static final Set<UUID> STAMPED_ENTITIES =
            Collections.synchronizedSet(new HashSet<>());

    public BlackStampItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user,
                                     LivingEntity entity, Hand hand) {
        if (entity instanceof PlayerEntity) return ActionResult.PASS;

        if (!user.getWorld().isClient) {
            UUID id = entity.getUuid();
            if (STAMPED_ENTITIES.contains(id)) {
                user.sendMessage(Text.literal("§7Already stamped."), true);
                return ActionResult.FAIL;
            }
            STAMPED_ENTITIES.add(id);

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
