package com.groomapack;

import com.groomapack.client.BrokerRenderer;
import com.groomapack.client.GravelWraithRenderer;
import com.groomapack.client.LurcherRenderer;
import com.groomapack.client.SootHoundRenderer;
import com.groomapack.client.TetoucherRenderer;
import com.groomapack.registry.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * The CLIENT-ONLY entry point for Groomapack.
 *
 * Fabric calls onInitializeClient() only on the player's game, never on a
 * dedicated server. All rendering registrations live here.
 */
public class KayAndCarlClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KayAndCarl.LOGGER.info("Groomapack client features loading...");

        // Entity renderers — each borrows a vanilla model until custom art is ready.
        EntityRendererRegistry.register(ModEntityTypes.TETOUCHER,    TetoucherRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.LURCHER,      LurcherRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GRAVEL_WRAITH,GravelWraithRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOOT_HOUND,   SootHoundRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.BROKER,       BrokerRenderer::new);
    }
}
