package com.groomapack.worldgen.feature;

import com.groomapack.registry.ModBlocks;
import com.groomapack.registry.ModEntityTypes;
import com.groomapack.registry.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FeatureContext;

/**
 * Kay's Basement — the hand-crafted dungeon room that generates underground.
 *
 * Uses the Feature API (simpler than Structure API — no NBT serialization, no
 * StructurePiece type registration). Placed by the biome-assigned placed feature
 * JSON (data/groomapack/worldgen/placed_feature/kays_basement.json).
 *
 * Room layout (13 W × 7 H × 13 D):
 *   • Shell: Deepslate Bricks walls, Polished Deepslate floor, Deepslate Tiles ceiling
 *   • Interior: Lanterns every 4 blocks on ceiling
 *   • Back wall: Chest with Kay's 24 Inch + starting loot
 *   • Front: Rig Beacon
 *   • Side: Foundry Block
 *   • 2–3 Tetouchers (persistent) + 1 Broker (persistent, guarding chest)
 *
 * Rarity: approximately 1 per 150 chunks (see placed_feature JSON), underground
 * between Y=-40 and Y=-10 in any overworld biome.
 *
 * The actual placement lives in {@link #placeRoom} so it can be reused by the
 * {@code /groomapack structure basement} command, which builds the room on demand
 * at the player's position.
 */
public class KaysBasementFeature extends Feature<DefaultFeatureConfig> {

    public static final int W = 13;
    public static final int H = 7;
    public static final int D = 13;

    public KaysBasementFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        ServerWorldAccess world = ctx.getWorld();
        BlockPos origin = ctx.getOrigin();
        Random random = ctx.getRandom();

        // Basic validity check during worldgen: don't carve into air or fluid.
        if (world.getBlockState(origin).isAir()
                || world.getBlockState(origin).getFluidState().isStill()) {
            return false;
        }

        placeRoom(world, origin, random);
        return true;
    }

    /**
     * Builds the entire room at {@code origin} (its low-NW-bottom corner).
     * Reused by both world generation and the structure command.
     */
    public static void placeRoom(ServerWorldAccess world, BlockPos origin, Random random) {
        placeShell(world, origin);
        placeFurnishings(world, origin);
        spawnMobs(world, origin, random);
    }

    private static void placeShell(ServerWorldAccess world, BlockPos origin) {
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                for (int z = 0; z < D; z++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean wall    = x == 0 || x == W-1 || z == 0 || z == D-1;
                    boolean floor   = y == 0;
                    boolean ceiling = y == H-1;

                    if (floor) {
                        world.setBlockState(p, Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);
                    } else if (ceiling) {
                        world.setBlockState(p, Blocks.DEEPSLATE_TILES.getDefaultState(), 3);
                    } else if (wall) {
                        world.setBlockState(p, Blocks.DEEPSLATE_BRICKS.getDefaultState(), 3);
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
        // Ceiling lanterns
        for (int x = 2; x < W-1; x += 4) {
            for (int z = 2; z < D-1; z += 4) {
                world.setBlockState(origin.add(x, H-2, z), Blocks.LANTERN.getDefaultState(), 3);
            }
        }
    }

    private static void placeFurnishings(ServerWorldAccess world, BlockPos origin) {
        // Chest at back-centre with starter loot.
        BlockPos chestPos = origin.add(W/2, 1, D-2);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModItems.KAYS_24_INCH));
            chest.setStack(1, new ItemStack(ModItems.RAW_CORE_DUST, 12));
            chest.setStack(2, new ItemStack(ModItems.SHARPENING_STONE, 3));
            chest.setStack(3, new ItemStack(ModItems.BLACK_STAMP, 4));
            chest.setStack(4, new ItemStack(ModItems.CORE_CELL, 3));
        }

        // Rig Beacon at front-centre.
        world.setBlockState(origin.add(W/2, 1, 1), ModBlocks.RIG_BEACON.getDefaultState(), 3);

        // Foundry Block at mid-left.
        world.setBlockState(origin.add(2, 1, D/2), ModBlocks.FOUNDRY_BLOCK.getDefaultState(), 3);
    }

    private static void spawnMobs(ServerWorldAccess world, BlockPos origin, Random random) {
        // Two guaranteed Tetouchers.
        spawnTetoucher(world, origin.add(3, 1, 4), random);
        spawnTetoucher(world, origin.add(W-4, 1, D-4), random);
        // Optional third.
        if (random.nextBoolean()) {
            spawnTetoucher(world, origin.add(W/2, 1, D/2), random);
        }
        // The Broker guards the chest.
        var broker = ModEntityTypes.BROKER.create(world.toServerWorld());
        if (broker != null) {
            broker.refreshPositionAndAngles(
                    origin.getX() + W/2 + 0.5, origin.getY() + 1, origin.getZ() + D - 3 + 0.5,
                    180, 0);
            broker.setPersistent();
            world.spawnEntity(broker);
        }
    }

    private static void spawnTetoucher(ServerWorldAccess world, BlockPos pos, Random random) {
        var t = ModEntityTypes.TETOUCHER.create(world.toServerWorld());
        if (t == null) return;
        t.refreshPositionAndAngles(
                pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                random.nextFloat() * 360, 0);
        t.setPersistent();
        world.spawnEntity(t);
    }
}
