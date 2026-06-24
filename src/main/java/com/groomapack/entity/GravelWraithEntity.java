package com.groomapack.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * The Gravel Wraith — flat, drifting hostile that rises from loose stone.
 *
 * Lore: mineral-lattice entities born in deep caves; they flicker like static
 * and dissolve into gravel when threatened. The Phantom Grit they drop is
 * their compressed mineral body.
 *
 * Mechanics:
 *   • Flies (FlyingEntity + BirdNavigation) — drifts toward the player
 *   • Immune to projectiles (arrows, snowballs, tridents pass through)
 *   • Contact damage 12, no bleed, no tricks — just relentless approach
 *   • Drops Phantom Grit (loot table)
 */
public class GravelWraithEntity extends FlyingEntity {

    public GravelWraithEntity(EntityType<? extends FlyingEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 45.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(false);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        // WanderAroundGoal works fine with BirdNavigation for idle floating.
        this.goalSelector.add(2, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /** Arrows, snowballs, and all projectiles pass right through. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isIn(DamageTypeTags.IS_PROJECTILE) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource src) {
        return false; // flying entity never takes fall damage
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PHANTOM_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ENTITY_PHANTOM_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PHANTOM_DEATH; }
}
