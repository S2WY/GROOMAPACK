package com.groomapack.client;

import com.groomapack.entity.CoreGrenadeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * Core Grenade renders as a spinning item icon (same as a thrown snowball).
 * FlyingItemEntityRenderer handles this automatically using getDefaultItem().
 */
public class CoreGrenadeRenderer extends FlyingItemEntityRenderer<CoreGrenadeEntity> {

    public CoreGrenadeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
}
