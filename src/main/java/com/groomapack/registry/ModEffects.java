package com.groomapack.registry;

import com.groomapack.KayAndCarl;
import com.groomapack.effect.ConfusedEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Central place where every custom STATUS EFFECT is registered:
 *   - Compression (stacks to 3, then a knockback "pop" — from The Lungbreaker)
 *   - Bleed (2 damage/sec for 6s, stackable — from the Serrated Fang)
 *   - Confused (the Tetoucher's 3-second freeze when you remove your boots)
 *
 * Only CONFUSED is implemented so far — it ships in the same step as the
 * Tetoucher, the mob that uses it. Compression and Bleed arrive alongside the
 * weapons that apply them.
 *
 * Registration pattern:
 *   1. A {@code RegistryEntry<StatusEffect>} field per effect. We store the
 *      RegistryEntry (not the bare StatusEffect) because that is exactly what
 *      {@code new StatusEffectInstance(...)} needs when we apply the effect.
 *   2. A private register(...) helper that calls Registry.registerReference.
 *   3. registerEffects() is called once from KayAndCarl.onInitialize().
 */
public class ModEffects {

    /**
     * Confused — HARMFUL category, indigo particle colour.
     *
     * We attach a movement-speed modifier of -100% (MULTIPLY_TOTAL with -1.0),
     * which alone would stop the mob dead. The Tetoucher's mobTick() ALSO clears
     * its target and stops its navigation while confused, so the freeze is
     * absolute — the speed modifier is the visible, reusable part of the effect,
     * and the AI override is the belt-and-suspenders guarantee.
     */
    public static final RegistryEntry<StatusEffect> CONFUSED = register(
            "confused",
            new ConfusedEffect(StatusEffectCategory.HARMFUL, 0x5A4FCF)
                    .addAttributeModifier(
                            EntityAttributes.GENERIC_MOVEMENT_SPEED,
                            KayAndCarl.id("confused_freeze"),
                            -1.0,
                            Operation.MULTIPLY_TOTAL));

    /**
     * registerReference returns a RegistryEntry, which is the handle the rest of
     * the mod uses to build StatusEffectInstances. (Plain Registry.register would
     * hand back the raw StatusEffect, forcing an extra lookup later.)
     */
    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, KayAndCarl.id(name), effect);
    }

    /**
     * Called from KayAndCarl.onInitialize(). Touching this class forces the
     * static field initialiser above to run, which performs the registration.
     */
    public static void registerEffects() {
        KayAndCarl.LOGGER.info("Registering Groomapack status effects (Confused)");
    }
}
