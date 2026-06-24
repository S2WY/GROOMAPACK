package com.groomapack.entity;

import com.groomapack.registry.ModItems;
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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Broker — a neutral trader found in Kay's Basement.
 *
 * Lore: The Broker is a... facilitator. He doesn't ask where you got the Black
 * Stamps. He doesn't care. He deals, you walk away satisfied, and neither of
 * you mentions the smell.
 *
 * Mechanics:
 *   • Neutral — ignores players unless attacked
 *   • Right-click to trade: offer a Black Stamp, receive a random rare item
 *   • If attacked, becomes permanently hostile (remembers via NBT flag)
 *   • Doesn't spawn naturally; only in Kay's Basement (see KaysBasementStructure)
 *   • Has the highest HP of any humanoid mob (80) and hits hard (20 damage)
 *
 * Trades (one Black Stamp each):
 *   Slot 0 → Core Cell (×3)
 *   Slot 1 → Sharpening Stone (×2)
 *   Slot 2 → Void Tape (×4)
 *   Slot 3 → Ember Coating (×2)
 *   Slot 4 → Carl's Arm (×1)
 *
 * Trade is randomly chosen each time. Future: full merchant GUI.
 */
public class BrokerEntity extends PathAwareEntity {

    private static final String KEY_HOSTILE = "BrokerHostile";
    private boolean permanentlyHostile = false;

    // The five trade outputs, cycling randomly.
    private static final ItemStack[] TRADE_OUTPUTS = {
            new ItemStack(ModItems.CORE_CELL, 3),
            new ItemStack(ModItems.SHARPENING_STONE, 2),
            new ItemStack(ModItems.VOID_TAPE, 4),
            new ItemStack(ModItems.EMBER_COATING, 2),
            new ItemStack(ModItems.CARL_ARM, 1),
    };

    public BrokerEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 30.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        // Only targets after being hit (RevengeGoal) or if permanently hostile.
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true,
                target -> this.permanentlyHostile));
    }

    /**
     * Right-click with a Black Stamp → give a random trade output.
     * Right-click with anything else → flavor text (Broker line).
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.permanentlyHostile || this.getWorld().isClient) {
            return ActionResult.PASS;
        }

        ItemStack held = player.getStackInHand(hand);
        if (held.isOf(ModItems.BLACK_STAMP)) {
            if (!player.getAbilities().creativeMode) held.decrement(1);
            ItemStack reward = TRADE_OUTPUTS[this.getRandom().nextInt(TRADE_OUTPUTS.length)].copy();
            if (!player.getInventory().insertStack(reward)) {
                player.dropItem(reward, false);
            }
            player.sendMessage(Text.literal(
                    "§7The Broker: §o\"Pleasure doing business.\""), false);
            this.playSound(SoundEvents.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
            return ActionResult.SUCCESS;
        }

        // Flavour line when you talk to him without stamps.
        player.sendMessage(Text.literal(
                "§7The Broker: §o\"Bring me Black Stamps. We have nothing to discuss.\""), false);
        return ActionResult.SUCCESS;
    }

    /** First hit makes him permanently hostile in this lifetime (not saved across worlds). */
    @Override
    public void onAttacking(net.minecraft.entity.Entity target) {
        super.onAttacking(target);
        this.permanentlyHostile = true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean(KEY_HOSTILE, permanentlyHostile);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.permanentlyHostile = nbt.getBoolean(KEY_HOSTILE);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_VILLAGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ENTITY_VILLAGER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_VILLAGER_DEATH; }
}
