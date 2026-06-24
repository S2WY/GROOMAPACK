package com.groomapack.entity;

import com.groomapack.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * The Lurcher — Groomapack's fast swarmer mob.
 *
 * Lore: leftover bodies from Kay's experiments. They move in packs, chattering
 * and lurching, individually weak but terrifying in numbers.
 *
 * Mechanics:
 *   • Fast (0.38 speed — sprints, noticeably quicker than a player)
 *   • Low HP (30) — easy one-on-one, but they spawn 2-4 at a time
 *   • Medium damage (8) — two hits kills an unarmoured player
 *   • Each bite has a 40% chance to apply Bleed I for 4 seconds
 *   • No special tricks — pure aggression and speed
 */
public class LurcherEntity extends HostileEntity {

    public LurcherEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.6);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /** 40% chance to inflict Bleed I (4 seconds) on each successful hit. */
    @Override
    public boolean tryAttack(net.minecraft.entity.Entity target) {
        boolean result = super.tryAttack(target);
        if (result && !this.getWorld().isClient
                && target instanceof net.minecraft.entity.LivingEntity living
                && this.getRandom().nextFloat() < 0.4f) {
            living.addStatusEffect(new StatusEffectInstance(ModEffects.BLEED, 80, 0));
        }
        return result;
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ENTITY_ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_ZOMBIE_DEATH; }
}
