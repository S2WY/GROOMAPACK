package com.groomapack.client;

import com.groomapack.entity.LurcherEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** Lurcher uses the zombie biped model — hunched, frantic movement. */
public class LurcherRenderer extends MobEntityRenderer<LurcherEntity, BipedEntityModel<LurcherEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/lurcher.png");

    public LurcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(LurcherEntity entity) { return TEXTURE; }
}
