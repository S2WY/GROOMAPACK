package com.groomapack;

import net.fabricmc.api.ClientModInitializer;

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

        // Coming in later steps, for example:
        //   EntityRendererRegistry.register(ModEntityTypes.TETOUCHER, TetoucherRenderer::new);
        //   HandledScreens.register(ModScreenHandlers.FOUNDRY, FoundryScreen::new);
        //   KeyBindingHelper.registerKeyBinding(MECH_BLAST_KEY);
    }
}
