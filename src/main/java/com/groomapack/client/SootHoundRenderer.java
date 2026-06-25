package com.groomapack.client;

import com.groomapack.entity.SootHoundEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** Soot Hound renders using a zombie biped model (placeholder until a custom model is made). */
public class SootHoundRenderer extends MobEntityRenderer<SootHoundEntity, BipedEntityModel<SootHoundEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/soot_hound.png");

    public SootHoundRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(SootHoundEntity entity) { return TEXTURE; }
}
