package com.groomapack.client;

import com.groomapack.entity.BrokerEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * The Broker uses the zombie biped model as a placeholder for a tall, cloaked
 * humanoid figure. A custom model is a polish step; lore-wise the silhouette
 * already reads as "unnervingly still humanoid."
 */
public class BrokerRenderer extends MobEntityRenderer<BrokerEntity, BipedEntityModel<BrokerEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/broker.png");

    public BrokerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(BrokerEntity entity) { return TEXTURE; }
}
