package com.groomapack.client;

import com.groomapack.entity.TheRigEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * The Rig renders using a zombie biped model (placeholder until a custom model is made).
 * A proper mech/golem model with pilot cockpit is planned for a later polish step.
 */
public class TheRigRenderer extends MobEntityRenderer<TheRigEntity, BipedEntityModel<TheRigEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/the_rig.png");

    public TheRigRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.7f);
    }

    @Override
    public Identifier getTexture(TheRigEntity entity) { return TEXTURE; }
}
