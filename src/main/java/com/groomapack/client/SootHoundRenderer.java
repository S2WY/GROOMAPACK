package com.groomapack.client;

import com.groomapack.entity.SootHoundEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.util.Identifier;

/** Soot Hound uses the wolf model — most natural fit for a canine. */
public class SootHoundRenderer extends MobEntityRenderer<SootHoundEntity, WolfEntityModel<SootHoundEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/soot_hound.png");

    public SootHoundRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WolfEntityModel<>(ctx.getPart(EntityModelLayers.WOLF)), 0.5f);
    }

    @Override
    public Identifier getTexture(SootHoundEntity entity) { return TEXTURE; }
}
