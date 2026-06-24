package com.groomapack.command;

import com.groomapack.KayAndCarl;
import com.groomapack.registry.ModBlocks;
import com.groomapack.registry.ModEntityTypes;
import com.groomapack.registry.ModItems;
import com.groomapack.worldgen.feature.KaysBasementFeature;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers the {@code /groomapack} command tree — a one-stop testing menu so
 * every item, mob, and the dungeon are reachable with tab-completion.
 *
 * <pre>
 *   /groomapack item &lt;name&gt; [count]   give yourself any Groomapack item/block/egg
 *   /groomapack mob  &lt;name&gt;           summon any Groomapack mob where you stand
 *   /groomapack structure basement   build Kay's Basement around your position
 * </pre>
 *
 * Requires permission level 2 (the usual bar for cheat-style commands), so it
 * works in singleplayer with cheats on, or for operators on a server.
 */
public final class ModCommands {

    private ModCommands() {}

    public static void register() {
        KayAndCarl.LOGGER.info("Registering /groomapack command");
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, access, environment) -> build(dispatcher));
    }

    /** Every obtainable item/block/egg, keyed by the name used in the command. */
    private static Map<String, Item> items() {
        Map<String, Item> m = new LinkedHashMap<>();
        m.put("kays_24_inch",     ModItems.KAYS_24_INCH);
        m.put("raw_core_dust",    ModItems.RAW_CORE_DUST);
        m.put("core_cell",        ModItems.CORE_CELL);
        m.put("core_grenade",     ModItems.CORE_GRENADE);
        m.put("sharpening_stone", ModItems.SHARPENING_STONE);
        m.put("ember_coating",    ModItems.EMBER_COATING);
        m.put("void_tape",        ModItems.VOID_TAPE);
        m.put("black_stamp",      ModItems.BLACK_STAMP);
        m.put("ember_dust",       ModItems.EMBER_DUST);
        m.put("phantom_grit",     ModItems.PHANTOM_GRIT);
        m.put("tetoucher_leather",ModItems.TETOUCHER_LEATHER);
        m.put("tetoucher_bone",   ModItems.TETOUCHER_BONE);
        m.put("shoearm",          ModItems.SHOEARM);
        m.put("carl_arm",         ModItems.CARL_ARM);
        m.put("carl_leg",         ModItems.CARL_LEG);
        m.put("carl_core",        ModItems.CARL_CORE);
        // Blocks (as items)
        m.put("void_tape_block",  ModBlocks.VOID_TAPE_BLOCK.asItem());
        m.put("foundry_block",    ModBlocks.FOUNDRY_BLOCK.asItem());
        m.put("rig_beacon",       ModBlocks.RIG_BEACON.asItem());
        // Spawn eggs
        m.put("tetoucher_spawn_egg",     ModItems.TETOUCHER_SPAWN_EGG);
        m.put("lurcher_spawn_egg",       ModItems.LURCHER_SPAWN_EGG);
        m.put("gravel_wraith_spawn_egg", ModItems.GRAVEL_WRAITH_SPAWN_EGG);
        m.put("soot_hound_spawn_egg",    ModItems.SOOT_HOUND_SPAWN_EGG);
        m.put("broker_spawn_egg",        ModItems.BROKER_SPAWN_EGG);
        m.put("the_rig_spawn_egg",       ModItems.THE_RIG_SPAWN_EGG);
        return m;
    }

    /** Every summonable mob, keyed by the name used in the command. */
    private static Map<String, EntityType<?>> mobs() {
        Map<String, EntityType<?>> m = new LinkedHashMap<>();
        m.put("tetoucher",     ModEntityTypes.TETOUCHER);
        m.put("lurcher",       ModEntityTypes.LURCHER);
        m.put("gravel_wraith", ModEntityTypes.GRAVEL_WRAITH);
        m.put("soot_hound",    ModEntityTypes.SOOT_HOUND);
        m.put("broker",        ModEntityTypes.BROKER);
        m.put("the_rig",       ModEntityTypes.THE_RIG);
        return m;
    }

    private static void build(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> root =
                literal("groomapack").requires(src -> src.hasPermissionLevel(2));

        // ---- /groomapack item <name> [count] ----
        LiteralArgumentBuilder<ServerCommandSource> itemNode = literal("item");
        for (Map.Entry<String, Item> e : items().entrySet()) {
            String name = e.getKey();
            Item item = e.getValue();
            itemNode.then(literal(name)
                    .executes(ctx -> giveItem(ctx.getSource(), name, item, 1))
                    .then(argument("count", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> giveItem(ctx.getSource(), name, item,
                                    IntegerArgumentType.getInteger(ctx, "count")))));
        }
        root.then(itemNode);

        // ---- /groomapack mob <name> ----
        LiteralArgumentBuilder<ServerCommandSource> mobNode = literal("mob");
        for (Map.Entry<String, EntityType<?>> e : mobs().entrySet()) {
            String name = e.getKey();
            EntityType<?> type = e.getValue();
            mobNode.then(literal(name)
                    .executes(ctx -> summonMob(ctx.getSource(), name, type)));
        }
        root.then(mobNode);

        // ---- /groomapack structure basement ----
        root.then(literal("structure").then(literal("basement")
                .executes(ctx -> buildBasement(ctx.getSource()))));

        dispatcher.register(root);
    }

    private static int giveItem(ServerCommandSource source, String name, Item item, int count)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ItemStack stack = new ItemStack(item, count);
        player.getInventory().offerOrDrop(stack);
        source.sendFeedback(() -> Text.literal("Gave " + count + " × " + name), false);
        return count;
    }

    private static int summonMob(ServerCommandSource source, String name, EntityType<?> type) {
        ServerWorld world = source.getWorld();
        Vec3d pos = source.getPosition();
        float yaw = source.getRotation().y;

        Entity entity = type.create(world);
        if (entity == null) {
            source.sendError(Text.literal("Could not create " + name));
            return 0;
        }
        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, 0.0f);
        world.spawnEntity(entity);
        source.sendFeedback(() -> Text.literal("Summoned " + name), false);
        return 1;
    }

    private static int buildBasement(ServerCommandSource source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld world = source.getWorld();
        BlockPos feet = player.getBlockPos();

        // Centre the room on the player: floor one block below their feet.
        BlockPos origin = new BlockPos(
                feet.getX() - KaysBasementFeature.W / 2,
                feet.getY() - 1,
                feet.getZ() - KaysBasementFeature.D / 2);

        KaysBasementFeature.placeRoom(world, origin, world.getRandom());
        source.sendFeedback(() -> Text.literal("Built Kay's Basement around you."), true);
        return 1;
    }
}
