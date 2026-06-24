package com.groomapack.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.math.Vec3d;

/**
 * Compression — a stacking crowd-control effect applied by The Lungbreaker.
 *
 * Design: each hit with The Lungbreaker applies one stack (amplifier++). At
 * amplifier 2 (the third hit), the compressed energy releases as a violent
 * knockback "pop". The victim is launched into the air and the effect is
 * cleared immediately.
 *
 *   amplifier 0 → "Compressed I"  — minor movement penalty, no pop yet
 *   amplifier 1 → "Compressed II" — noticeable stagger
 *   amplifier 2 → "Compressed III" → POP: huge upward + outward knockback,
 *                                      effect removed immediately
 *
 * The stackable progression uses Minecraft's built-in behaviour: applying the
 * effect with a higher amplifier on an entity that already has a lower one
 * replaces it (vanilla StatusEffectInstance merging logic). The weapon code is
 * responsible for always applying amplifier 0; the stacking happens because
 * vanilla DOES upgrade the amplifier when the new one is higher.
 *
 * Wait — that's wrong. Vanilla replaces if duration is longer OR amplifier is
 * higher. So applying amplifier 0 twice WON'T stack. For real stacking the
 * weapon reads the current amplifier and applies amplifier+1. This is handled
 * in the weapon item's postHit() when The Lungbreaker is added.
 */
public class CompressionEffect extends StatusEffect {

    public CompressionEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Called each tick this effect is active. At amplifier 2 we trigger the pop:
     * a violent launch. The "fire once and vanish" behaviour comes from the
     * applier — the (future) weapon applies Compression III with a 1-tick
     * duration, so the launch happens on the only active tick and vanilla then
     * removes the expired effect. (We deliberately do NOT self-remove from inside
     * the tick, which would mutate the active-effects map mid-iteration.)
     */
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (amplifier >= 2) {
            // Violent upward launch + a small random horizontal component
            double vx = (entity.getRandom().nextDouble() - 0.5) * 1.2;
            double vy = 1.4;
            double vz = (entity.getRandom().nextDouble() - 0.5) * 1.2;
            entity.setVelocity(new Vec3d(vx, vy, vz));
            entity.velocityModified = true; // force the new velocity to sync to the client
        }
        return true;
    }

    /** Pop fires while at amplifier 2 (applied with a 1-tick duration → once). */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return amplifier >= 2 && duration > 0;
    }
}
