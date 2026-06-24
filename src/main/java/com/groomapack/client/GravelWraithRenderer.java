package com.groomapack.client;

import com.groomapack.entity.GravelWraithEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.util.Identifier;

/** Gravel Wraith uses the phantom model — flat, wide, drifting. */
public class GravelWraithRenderer extends MobEntityRenderer<GravelWraithEntity, PhantomEntityModel<GravelWraithEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/gravel_wraith.png");

    public GravelWraithRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PhantomEntityModel<>(ctx.getPart(EntityModelLayers.PHANTOM)), 0.75f);
    }

    @Override
    public Identifier getTexture(GravelWraithEntity entity) { return TEXTURE; }
}
