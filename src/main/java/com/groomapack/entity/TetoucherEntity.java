package com.groomapack.entity;

import com.groomapack.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * The Tetoucher — Groomapack's first and signature mob.
 *
 * A tall, lanky horror that stalks the player by their FOOTWEAR. Design goals:
 *
 *   • DEADLY MELEE      — hits as hard as the Warden (~30 damage). One mistake
 *                         is a near-death experience.
 *   • RELENTLESS        — climbs walls (spider-style) and opens/paths through
 *                         doors, so you cannot simply wall it out.
 *   • TELEPORTS         — if you open a gap of more than 15 blocks, it blinks
 *                         back into range. Running never fully works.
 *   • ONE WEAKNESS      — take your boots off. A barefoot player "confuses" it,
 *                         freezing it for 3 seconds (see {@link ModEffects#CONFUSED}).
 *
 * CODE SECTIONS:
 *   1. Tracked data + fields
 *   2. Construction + attributes
 *   3. AI goals + navigation (door pathing)
 *   4. Wall climbing
 *   5. mobTick — confusion, boot detection, teleport
 *   6. Teleport helpers
 *   7. Sounds
 */
public class TetoucherEntity extends HostileEntity {

    // =========================================================================
    // 1. TRACKED DATA + FIELDS
    // =========================================================================

    /**
     * A single byte, synced from server to client, whose lowest bit means
     * "currently climbing a wall". We copy the exact trick the vanilla Spider
     * uses. The client needs this so {@link #isClimbing()} can return true and
     * the entity sticks to walls visually instead of sliding off.
     */
    private static final TrackedData<Byte> CLIMBING =
            DataTracker.registerData(TetoucherEntity.class, TrackedDataHandlerRegistry.BYTE);

    /** Ticks remaining before the Tetoucher may teleport again. */
    private int teleportCooldown = 0;

    /** Throttles the (slightly expensive) "is a nearby player barefoot?" scan. */
    private int bootScanCooldown = 0;

    // =========================================================================
    // 2. CONSTRUCTION + ATTRIBUTES
    // =========================================================================

    public TetoucherEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Base stats. Registered with Fabric in ModEntityTypes so they apply to
     * every Tetoucher that spawns.
     *
     *   MAX_HEALTH 100        — a mini-boss; tougher than any vanilla overworld mob
     *                           but well short of the Warden's 500.
     *   MOVEMENT_SPEED 0.32   — noticeably faster than a player's walk (0.1 base
     *                           attribute ≈ player); it WILL catch a walker.
     *   ATTACK_DAMAGE 30      — "warden-level". Brutal on purpose.
     *   FOLLOW_RANGE 40       — sees and pursues from far away.
     *   KNOCKBACK_RESISTANCE  — hard to shove off; it keeps coming.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 30.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CLIMBING, (byte) 0);
    }

    // =========================================================================
    // 3. AI GOALS + NAVIGATION
    // =========================================================================

    /**
     * goalSelector = what the mob DOES (move, attack, wander, look).
     * targetSelector = who the mob HATES (who it will pursue).
     * Lower priority number = considered first.
     */
    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        // Chase + strike. 1.0 = move at full speed toward the target.
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        // When idle, roam so it doesn't stand frozen.
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        // Creepy "it's watching you" head tracking.
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));

        // Hit it and it hits back; otherwise it hunts the nearest player.
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /**
     * Use a ground navigator that is allowed to route THROUGH doorways. Combined
     * with the door-toggling behaviour below, this is what makes "hide behind a
     * door" fail against the Tetoucher.
     */
    @Override
    protected EntityNavigation createNavigation(World world) {
        MobNavigation navigation = new MobNavigation(this, world);
        // Treat doorways as traversable so the Tetoucher routes through them
        // instead of being walled out by a closed door.
        navigation.setCanPathThroughDoors(true);
        navigation.setCanSwim(true);
        return navigation;
    }

    // =========================================================================
    // 4. WALL CLIMBING (spider behaviour)
    // =========================================================================

    @Override
    public void tick() {
        super.tick();
        // Server decides the climbing state; clients just read the synced byte.
        if (!this.getWorld().isClient) {
            this.setClimbingWall(this.horizontalCollision);
        }
    }

    /**
     * isClimbing() is consulted by the movement code: when true, running into a
     * wall makes the entity stick and ascend instead of stopping.
     */
    @Override
    public boolean isClimbing() {
        return this.isClimbingWall();
    }

    public boolean isClimbingWall() {
        return (this.dataTracker.get(CLIMBING) & 1) != 0;
    }

    public void setClimbingWall(boolean climbing) {
        byte flags = this.dataTracker.get(CLIMBING);
        flags = climbing ? (byte) (flags | 1) : (byte) (flags & ~1);
        this.dataTracker.set(CLIMBING, flags);
    }

    // =========================================================================
    // 5. MOB TICK — confusion, boot detection, teleport
    // =========================================================================

    /**
     * Runs every server tick for this mob. Order is deliberate:
     *   A) If already confused, hard-freeze and bail out.
     *   B) Once a second, scan for a barefoot player → become confused.
     *   C) Otherwise, if the target slipped beyond 15 blocks, teleport closer.
     */
    @Override
    protected void mobTick() {
        super.mobTick();

        // A) CONFUSED → total freeze.
        if (this.hasStatusEffect(ModEffects.CONFUSED)) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setVelocity(0.0, this.getVelocity().y, 0.0);
            this.velocityModified = true; // sync the zero-horizontal velocity to clients
            return;
        }

        // B) BOOT DETECTION. A player with an empty FEET slot within 16 blocks
        //    confuses the Tetoucher for 3 seconds (60 ticks) and breaks pursuit.
        if (--this.bootScanCooldown <= 0) {
            this.bootScanCooldown = 20; // re-scan once per second
            PlayerEntity nearest = this.getWorld().getClosestPlayer(this, 16.0);
            if (nearest != null
                    && !nearest.isSpectator()
                    && !nearest.isCreative()
                    && nearest.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
                this.addStatusEffect(new StatusEffectInstance(ModEffects.CONFUSED, 60, 0));
                this.setTarget(null);
                this.playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 1.0f, 0.6f);
                return;
            }
        }

        // C) TELEPORT. If the quarry opens a gap of more than 15 blocks, blink.
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.teleportCooldown > 0) {
                this.teleportCooldown--;
            }
            double distanceSq = this.squaredDistanceTo(target);
            if (distanceSq > 15.0 * 15.0 && this.teleportCooldown == 0) {
                if (this.teleportTowards(target)) {
                    this.teleportCooldown = 80; // 4-second breather between blinks
                }
            }
        }
    }

    // =========================================================================
    // 6. TELEPORT HELPERS (enderman-style, with a safe-landing check)
    // =========================================================================

    /** Picks a random spot within ~3 blocks of the target and tries to blink there. */
    private boolean teleportTowards(LivingEntity target) {
        double x = target.getX() + (this.random.nextDouble() - 0.5) * 6.0;
        double y = target.getY() + this.random.nextInt(4) - 1;
        double z = target.getZ() + (this.random.nextDouble() - 0.5) * 6.0;
        return teleportTo(x, y, z);
    }

    /**
     * Drops the candidate position down to the first solid, non-water block and
     * teleports onto it if the landing is valid. Returns false (so we can retry
     * next tick) if there's nowhere safe to stand.
     */
    private boolean teleportTo(double x, double y, double z) {
        BlockPos.Mutable pos = BlockPos.ofFloored(x, y, z).mutableCopy();
        while (pos.getY() > this.getWorld().getBottomY()
                && !this.getWorld().getBlockState(pos).blocksMovement()) {
            pos.move(Direction.DOWN);
        }

        var groundState = this.getWorld().getBlockState(pos);
        boolean solidGround = groundState.blocksMovement();
        boolean inWater = groundState.getFluidState().isIn(FluidTags.WATER);
        if (!solidGround || inWater) {
            return false;
        }

        double fromX = this.getX();
        double fromY = this.getY();
        double fromZ = this.getZ();
        boolean moved = this.teleport(x, pos.getY() + 1.0, z, true);
        if (moved) {
            this.getWorld().playSound(null, fromX, fromY, fromZ,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 1.0f);
            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
        return moved;
    }

    // =========================================================================
    // 7. SOUNDS (enderman placeholders — themed: tall, teleporting, eerie)
    // =========================================================================

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }
}
