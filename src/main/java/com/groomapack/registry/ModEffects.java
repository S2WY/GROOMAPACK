package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.effect.BleedEffect;
import com.groomapack.effect.CompressionEffect;
import com.groomapack.effect.ConfusedEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * All Groomapack status effects, registered here and wired to the mobs/weapons
 * that apply them.
 *
 *   CONFUSED     — Tetoucher's panic when the player is barefoot (3-second freeze)
 *   BLEED        — stacking damage-over-time from sharp weapons (2/4/6 dmg/sec)
 *   COMPRESSION  — stacking knockback from The Lungbreaker; 3rd stack = pop
 */
public class ModEffects {

    /**
     * Confused — HARMFUL, indigo, -100% movement (total freeze).
     * AI hard-freeze enforced in TetoucherEntity.mobTick() on top of the stat.
     */
    public static final RegistryEntry<StatusEffect> CONFUSED = register(
            "confused",
            new ConfusedEffect(StatusEffectCategory.HARMFUL, 0x5A4FCF)
                    .addAttributeModifier(
                            EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            KayAndCarl.id("confused_freeze"),
                            -1.0,
                            Operation.ADD_MULTIPLIED_TOTAL));

    /**
     * Bleed — HARMFUL, dark red. Damage dealt in BleedEffect.applyUpdateEffect.
     * No attribute modifier needed; the tick handles the damage directly.
     */
    public static final RegistryEntry<StatusEffect> BLEED = register(
            "bleed",
            new BleedEffect(StatusEffectCategory.HARMFUL, 0xAA1111));

    /**
     * Compression — HARMFUL, ice-blue. Minor movement slow while stacking;
     * the pop at amplifier 2 is handled in CompressionEffect.applyUpdateEffect.
     */
    public static final RegistryEntry<StatusEffect> COMPRESSION = register(
            "compression",
            new CompressionEffect(StatusEffectCategory.HARMFUL, 0x3399FF)
                    .addAttributeModifier(
                            EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            KayAndCarl.id("compression_slow"),
                            -0.15,
                            Operation.ADD_MULTIPLIED_TOTAL));

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, KayAndCarl.id(name), effect);
    }

    public static void registerEffects() {
        KayAndCarl.LOGGER.info("Registering Groomapack status effects (Confused, Bleed, Compression)");
    }
}
