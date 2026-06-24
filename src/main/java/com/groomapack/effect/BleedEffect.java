package com.groomapack.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/**
 * Bleed — a stackable damage-over-time effect.
 *
 * Applied by sharp/serrated weapons. Each application increases the amplifier:
 *   amplifier 0 → 2 dmg/sec  (single bleed)
 *   amplifier 1 → 4 dmg/sec  (double bleed)
 *   amplifier 2 → 6 dmg/sec  (triple bleed — near-fatal over 6s)
 *
 * Duration: 120 ticks (6 seconds) per application.
 * Damage type: uses the world's generic.kill source so it doesn't interact with
 * armour differently — bleed is "internal" trauma, not physical hit.
 *
 * NOTE: We use the overrideable applyUpdateEffect / shouldApplyUpdateEffect
 * pattern that Minecraft already calls for Poison, Wither, Regeneration, etc.
 * No external event listeners needed.
 */
public class BleedEffect extends StatusEffect {

    public BleedEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Called when Minecraft decides it's time for a periodic effect application.
     * We deal (amplifier + 1) * 2 raw magic damage — bypasses armour.
     */
    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        float damage = (amplifier + 1) * 2.0f;
        DamageSource src = world.getDamageSources().magic();
        entity.damage(world, src, damage);
        return true;
    }

    /**
     * Controls the cadence. We want 1 tick per second (every 20 ticks).
     * Vanilla Poison fires at {@code 25 >> amplifier} but we keep a flat 20
     * so higher amplifiers don't fire faster — just hurt more per hit.
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
