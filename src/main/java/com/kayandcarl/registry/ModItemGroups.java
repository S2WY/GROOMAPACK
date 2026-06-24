package com.kayandcarl.registry;

import com.kayandcarl.KayAndCarl;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;

/**
 * Creates the "Groomapack" creative-inventory tab and fills it with our items.
 *
 * Without this, our items would still exist but you'd have to /give them by id.
 * A dedicated tab makes everything easy to find while testing.
 */
public class ModItemGroups {

    /**
     * The key that identifies our creative tab. We need this both to register
     * the group AND to tell Fabric "add these items into this specific tab".
     */
    public static final RegistryKey<ItemGroup> CODEX_GROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), KayAndCarl.id("codex"));

    /**
     * The tab itself. Its icon is Kay's 24 Inch, and its title text is looked
     * up from the language file (lang/en_us.json -> "itemgroup.kayandcarl.codex").
     */
    public static final ItemGroup CODEX_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            CODEX_GROUP_KEY,
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.KAYS_24_INCH))
                    .displayName(Text.translatable("itemgroup.kayandcarl.codex"))
                    .build()
    );

    public static void registerItemGroups() {
        KayAndCarl.LOGGER.info("Registering Groomapack creative tab");

        // This event fires when Minecraft is building the contents of our tab.
        // We add each item we want to appear. Order here = order shown in the GUI.
        ItemGroupEvents.modifyEntriesEvent(CODEX_GROUP_KEY).register(entries -> {
            entries.add(ModItems.KAYS_24_INCH);
            entries.add(ModItems.RAW_CORE_DUST);
            entries.add(ModItems.CORE_CELL);
            entries.add(ModItems.CORE_GRENADE);
            entries.add(ModItems.SHARPENING_STONE);
            entries.add(ModItems.EMBER_COATING);
            entries.add(ModItems.VOID_TAPE);
            entries.add(ModItems.BLACK_STAMP);
            entries.add(ModItems.EMBER_DUST);
            entries.add(ModItems.PHANTOM_GRIT);
            entries.add(ModItems.TETOUCHER_LEATHER);
            entries.add(ModItems.TETOUCHER_BONE);
            entries.add(ModItems.SHOEARM);
            entries.add(ModItems.CARL_ARM);
            entries.add(ModItems.CARL_LEG);
            entries.add(ModItems.CARL_CORE);
        });
    }
}
