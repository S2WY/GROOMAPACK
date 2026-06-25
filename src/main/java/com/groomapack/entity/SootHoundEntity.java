package com.groomapack.entity;

import com.groomapack.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Soot Hound — a tameable ember-coated canine.
 *
 * Lore: descended from Soot Hounds that Kay kept in the basement. Their fur
 * conducts heat; touching one unprotected leaves burn marks.
 *
 * Mechanics (untamed):
 *   • SpawnGroup.CREATURE — spawns like animals, surface biomes
 *   • Neutral: ignores players unless cornered (attacked), then retaliates
 *   • Tame: right-click with a Core Cell (10 % chance per use, like cat/wolf)
 *
 * Mechanics (tamed):
 *   • Follows its owner, sits on request (right-click to toggle)
 *   • Attacks anything that hurt its owner (RevengeGoal from owner's attacker)
 *   • "Ember Bite" — 25 % chance per hit to set the target on fire for 3 sec
 *   • Still drops Ember Dust on death
 */
public class SootHoundEntity extends TameableEntity {

    public SootHoundEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(4, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

        // Untamed: only attacks players that attack it first.
        // Tamed: also attacks those who hurt the owner (handled in onOwnerAttacked).
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true,
                target -> !this.isTamed()));
    }

    /**
     * Right-click with a Core Cell → 10 % tame chance. Any other right-click
     * on a tamed Soot Hound toggles sitting (same as wolf).
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        if (!this.isTamed()) {
            if (held.isOf(ModItems.CORE_CELL)) {
                if (!player.getAbilities().creativeMode) held.decrement(1);
                if (this.getRandom().nextFloat() < 0.1f) {
                    this.setOwner(player);
                    this.navigation.stop();
                    this.setHealth(this.getMaxHealth());
                    if (!this.getWorld().isClient) {
                        ((ServerWorld) this.getWorld()).spawnParticles(
                                ParticleTypes.HEART,
                                this.getX(), this.getEyeY(), this.getZ(),
                                7, 0.3, 0.3, 0.3, 0);
                        player.sendMessage(Text.literal("§6The Soot Hound is yours now."), true);
                    }
                } else {
                    if (!this.getWorld().isClient) {
                        ((ServerWorld) this.getWorld()).spawnParticles(
                                ParticleTypes.SMOKE,
                                this.getX(), this.getEyeY(), this.getZ(),
                                7, 0.3, 0.3, 0.3, 0);
                    }
                }
                return ActionResult.SUCCESS;
            }
        } else if (player.getUuid().equals(this.getOwnerUuid())) {
            if (!this.getWorld().isClient) {
                this.setSitting(!this.isSitting());
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    /** 25 % chance per successful hit to set the target on fire for 3 seconds. */
    @Override
    public boolean tryAttack(net.minecraft.entity.Entity target) {
        boolean result = super.tryAttack(target);
        if (result && !this.getWorld().isClient && this.getRandom().nextFloat() < 0.25f) {
            target.setOnFireFor(3);
        }
        return result;
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return !(target instanceof SootHoundEntity other && other.getOwner() == owner);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) { return false; }

    @Override
    public SootHoundEntity createChild(net.minecraft.server.world.ServerWorld world,
                                       net.minecraft.entity.passive.PassiveEntity entity) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WOLF_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ENTITY_WOLF_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WOLF_DEATH; }
}
