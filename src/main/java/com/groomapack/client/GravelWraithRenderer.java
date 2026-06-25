package com.groomapack.client;

import com.groomapack.entity.GravelWraithEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** Gravel Wraith renders using a zombie biped model (placeholder until a custom model is made). */
public class GravelWraithRenderer extends MobEntityRenderer<GravelWraithEntity, BipedEntityModel<GravelWraithEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/gravel_wraith.png");

    public GravelWraithRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.75f);
    }

    @Override
    public Identifier getTexture(GravelWraithEntity entity) { return TEXTURE; }
}
