package com.groomapack;

import com.groomapack.client.TetoucherRenderer;
import com.groomapack.registry.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * The CLIENT-ONLY entry point for Groomapack.
 *
 * Fabric calls {@link #onInitializeClient()} only on the player's actual game,
 * never on a dedicated server. Everything that touches rendering or the screen
 * lives here:
 *   - entity renderers (how the Tetoucher, Soot Hound, and Rig mech look)
 *   - GUI / Screen classes (the Foundry Block menu, Carl Toolbox menu)
 *   - block render layers (so glass-like or glowing blocks draw correctly)
 *   - key bindings (e.g. the mech's blast / mode-toggle keys)
 *
 * It is empty for now. As we add each visual feature we will register it here
 * and I'll explain it at that step.
 */
public class KayAndCarlClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KayAndCarl.LOGGER.info("Groomapack client features loading...");

        // Tell the game how to draw the Tetoucher. Reuses the vanilla Enderman
        // model for now (see TetoucherRenderer); a custom model comes later.
        EntityRendererRegistry.register(ModEntityTypes.TETOUCHER, TetoucherRenderer::new);

        // Coming in later steps, for example:
        //   HandledScreens.register(ModScreenHandlers.FOUNDRY, FoundryScreen::new);
        //   KeyBindingHelper.registerKeyBinding(MECH_BLAST_KEY);
    }
}
