package com.groomapack.client;

import com.groomapack.entity.TheRigEntity;
import com.groomapack.KayAndCarl;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.util.Identifier;

/**
 * The Rig uses the Iron Golem model — large, imposing, heavy-moving.
 * Fits the "Carl's mech" lore: massive, industrial, inexorable.
 * Custom model with pilot cockpit is a future polish step.
 */
public class TheRigRenderer extends MobEntityRenderer<TheRigEntity, IronGolemEntityModel<TheRigEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/the_rig.png");

    public TheRigRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IronGolemEntityModel<>(ctx.getPart(EntityModelLayers.IRON_GOLEM)), 0.7f);
    }

    @Override
    public Identifier getTexture(TheRigEntity entity) { return TEXTURE; }
}
