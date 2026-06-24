package com.groomapack.entity;

import com.groomapack.registry.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Rig — Carl's mech chassis that the player pilots.
 *
 * Lore: Carl is a digital-physical AI embedded in the Core Cell system. The Rig
 * is the physical body Carl occupies; it combines his processing with the player's
 * aim. Together they're near-unstoppable. Alone, it's a very heavy statue.
 *
 * MECHANICS:
 *   • Assembled at a Rig Beacon via Carl's Core
 *   • Player right-clicks to mount; sneak to dismount
 *   • While mounted: the player's WASD drives The Rig
 *   • The Rig's own melee is 40 damage (stomps); the player attacks normally too
 *   • Very slow (0.2 speed) but massive knockback resistance; almost immovable
 *   • 200 HP; repairable with Core Cells (right-click while dismounted)
 *
 * CARL VOICE (flavour messages at key moments):
 *   • On mount:     "Carl: \"Locked in. Let's be stupid.\"
 *   • On first hit: "Carl: \"We've taken a hit. Holding.\""
 *   • On low HP:    "Carl: \"Heavy damage. This is fine. Mostly.\""
 *   • On dismount:  "Carl: \"Good session. Come back soon.\""
 *
 * CONTROLS (while riding):
 *   Movement: standard WASD (rider designated via getControllingPassenger)
 *   Sneak key: dismount
 *
 * The boostCooldown field is retained for a planned jump-boost; wiring the jump
 * key requires a client input packet, so the launch itself is not yet active.
 */
public class TheRigEntity extends PathAwareEntity {

    private static final int BOOST_COOLDOWN_TICKS = 600; // 30 seconds
    private static final int REPAIR_PER_CELL = 40;
    private static final String KEY_BOOST_CD = "BoostCooldown";

    private int boostCooldown = 0;
    private boolean saidFirstHit = false;

    public TheRigEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setInvulnerable(false);
        this.setNoGravity(false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.95)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 0.0);  // no autonomous targeting
    }

    // ---------------------------------------------------------------- riding

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient) return ActionResult.SUCCESS;

        var held = player.getStackInHand(hand);

        // Repair: right-click with Core Cell while not riding.
        if (held.isOf(com.groomapack.registry.ModItems.CORE_CELL)
                && !this.hasPassengers()) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(REPAIR_PER_CELL);
                if (!player.getAbilities().creativeMode) held.decrement(1);
                player.sendMessage(Text.literal("§7Carl: §o\"Appreciated. Running better already.\""), true);
            } else {
                player.sendMessage(Text.literal("§7Carl: §o\"Full health. Save those cells.\""), true);
            }
            return ActionResult.SUCCESS;
        }

        // Mount: right-click with empty hand.
        if (held.isEmpty() && !this.hasPassengers()) {
            player.startRiding(this);
            player.sendMessage(Text.literal("§6Carl: §o\"Locked in. Let's be stupid.\""), false);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    /**
     * Player input is relayed here every tick while they're riding. We read
     * the rider's sideways/forward movement and apply it to The Rig's motion.
     * Vanilla calls this on the server after syncing client inputs.
     */
    @Override
    public void travel(Vec3d movementInput) {
        if (this.hasPassengers() && this.getFirstPassenger() instanceof PlayerEntity rider) {
            // Yaw follows the rider's look direction.
            this.setYaw(rider.getYaw());
            this.prevYaw = this.getYaw();

            // Forward/sideways from rider input (replicated to server automatically).
            float forward  = rider.forwardSpeed;
            float sideways = rider.sidewaysSpeed;
            double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);

            Vec3d movement = new Vec3d(sideways * speed, 0, forward * speed)
                    .rotateY(-this.getYaw() * ((float) Math.PI / 180F));
            this.move(MovementType.SELF, movement);
            this.updateLimbs(false);
        } else {
            super.travel(movementInput);
        }
    }

    /**
     * Designates the mounted player as the driver. With a non-null controlling
     * passenger, the client forwards WASD input and the server ticks travel(),
     * which is where we translate that input into motion.
     */
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof PlayerEntity p ? p : super.getControllingPassenger();
    }

    @Override
    protected boolean canAddPassenger(net.minecraft.entity.Entity passenger) {
        return !this.hasPassengers() && passenger instanceof PlayerEntity;
    }

    // ---------------------------------------------------------------- ticking

    @Override
    protected void mobTick() {
        super.mobTick();
        if (boostCooldown > 0) boostCooldown--;

        // Low-HP warning (once, below 20%).
        if (!this.getWorld().isClient
                && this.getHealth() < this.getMaxHealth() * 0.2f
                && this.hasPassengers()
                && this.getFirstPassenger() instanceof PlayerEntity rider) {
            rider.sendMessage(
                    Text.literal("§cCarl: §o\"Heavy damage. This is fine. Mostly.\""), true);
        }
    }

    @Override
    public void onPassengerLookAround(net.minecraft.entity.Entity passenger) {
        // Rider looks around freely; The Rig's yaw syncs in travel().
    }

    @Override
    public void removeAllPassengers() {
        if (!this.getWorld().isClient && this.hasPassengers()
                && this.getFirstPassenger() instanceof PlayerEntity rider) {
            rider.sendMessage(Text.literal("§7Carl: §o\"Good session. Come back soon.\""), false);
        }
        super.removeAllPassengers();
    }

    // ---------------------------------------------------------------- combat

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hit = super.damage(source, amount);
        if (hit && !saidFirstHit && this.hasPassengers()
                && this.getFirstPassenger() instanceof PlayerEntity rider) {
            saidFirstHit = true;
            rider.sendMessage(Text.literal("§7Carl: §o\"We've taken a hit. Holding.\""), true);
        }
        return hit;
    }

    // ---------------------------------------------------------------- NBT

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(KEY_BOOST_CD, boostCooldown);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.boostCooldown = nbt.getInt(KEY_BOOST_CD);
    }

    // ---------------------------------------------------------------- sound + look

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state) {
        this.playSound(SoundEvents.ENTITY_IRON_GOLEM_STEP, 1.0f, 0.9f);
    }
}
