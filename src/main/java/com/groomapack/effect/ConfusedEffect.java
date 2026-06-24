package com.groomapack.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Confused — the Tetoucher's panic state.
 *
 * Lore: the Tetoucher hunts by FOOTWEAR. If a player takes their boots off, the
 * creature loses its anchor on them and is briefly "confused" — it freezes for
 * 3 seconds, forgets its target, and stops moving. This is the player's one
 * reliable defensive trick against it.
 *
 * Why a whole class for this? Minecraft's StatusEffect constructor is
 * {@code protected}, so we cannot write {@code new StatusEffect(...)} directly.
 * The standard pattern is a tiny subclass that simply re-exposes the constructor
 * as {@code public}. All the actual behaviour (the movement-speed penalty) is
 * attached at registration time in {@link com.groomapack.registry.ModEffects},
 * and the hard "freeze" (clearing the target, zeroing navigation) is enforced in
 * {@link com.groomapack.entity.TetoucherEntity#mobTick()}.
 *
 * We keep this generic enough that other mobs could be "Confused" later too.
 */
public class ConfusedEffect extends StatusEffect {

    public ConfusedEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
