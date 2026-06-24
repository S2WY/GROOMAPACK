package com.groomapack;

import com.groomapack.client.BrokerRenderer;
import com.groomapack.client.CoreGrenadeRenderer;
import com.groomapack.client.GravelWraithRenderer;
import com.groomapack.client.LurcherRenderer;
import com.groomapack.client.SootHoundRenderer;
import com.groomapack.client.TetoucherRenderer;
import com.groomapack.client.TheRigRenderer;
import com.groomapack.registry.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * CLIENT-ONLY entry point for Groomapack.
 * Registers entity renderers and any other purely visual content.
 */
public class KayAndCarlClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KayAndCarl.LOGGER.info("Groomapack client features loading...");

        EntityRendererRegistry.register(ModEntityTypes.TETOUCHER,    TetoucherRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.LURCHER,      LurcherRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GRAVEL_WRAITH,GravelWraithRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOOT_HOUND,   SootHoundRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.BROKER,       BrokerRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.THE_RIG,      TheRigRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CORE_GRENADE, CoreGrenadeRenderer::new);
    }
}
