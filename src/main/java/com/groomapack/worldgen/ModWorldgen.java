package com.groomapack.worldgen;

import com.groomapack.KayAndCarl;
import com.groomapack.worldgen.feature.KaysBasementFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class ModWorldgen {

    public static final Feature<DefaultFeatureConfig> KAYS_BASEMENT = Registry.register(
            Registries.FEATURE,
            KayAndCarl.id("kays_basement"),
            new KaysBasementFeature(DefaultFeatureConfig.CODEC));

    public static void register() {
        KayAndCarl.LOGGER.info("Registering Groomapack worldgen");

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, KayAndCarl.id("kays_basement")));
    }
}
