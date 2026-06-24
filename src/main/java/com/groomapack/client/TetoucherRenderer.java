package com.groomapack.client;

import com.groomapack.KayAndCarl;
import com.groomapack.entity.TetoucherEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * How the Tetoucher is drawn.
 *
 * BASIC FOR NOW. Building a bespoke model + animation rig is its own large task,
 * so we borrow the vanilla Enderman geometry — a tall, lanky, long-limbed
 * silhouette that already fits the Tetoucher's "looming, teleporting horror"
 * brief. We just point it at our own texture.
 *
 *   • MobEntityRenderer<Entity, Model> is the standard base for mob rendering.
 *   • EndermanEntityModel is generic over any LivingEntity, so it accepts our
 *     TetoucherEntity directly.
 *   • ctx.getPart(EntityModelLayers.ENDERMAN) reuses the already-baked Enderman
 *     model layer — no need to register our own layer yet.
 *   • shadow radius 0.5f — a modest contact shadow.
 *
 * The texture lives at assets/groomapack/textures/entity/tetoucher.png. Until an
 * artist supplies one, Minecraft shows its magenta/black "missing texture"
 * placeholder — the entity still renders, moves, and fights correctly.
 *
 * A custom model + a proper texture are a later polish step.
 */
public class TetoucherRenderer extends MobEntityRenderer<TetoucherEntity, EndermanEntityModel<TetoucherEntity>> {

    private static final Identifier TEXTURE = KayAndCarl.id("textures/entity/tetoucher.png");

    public TetoucherRenderer(EntityRendererFactory.Context context) {
        super(context, new EndermanEntityModel<>(context.getPart(EntityModelLayers.ENDERMAN)), 0.5f);
    }

    @Override
    public Identifier getTexture(TetoucherEntity entity) {
        return TEXTURE;
    }
}
