package com.groomapack.item;

import com.groomapack.KayAndCarl;
import com.groomapack.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.groomapack.item.EmberCoatingItem;

// AttributeModifierSlot controls WHICH hand/slot the modifier applies to.
// In 1.21.1 Yarn it lives in the component package.
import net.minecraft.component.type.AttributeModifierSlot;

/**
 * Kay's 24 Inch — the central multi-tool of Groomapack.
 *
 * BEHAVIOUR SUMMARY:
 *   Default (Combat Mode):  attacks at netherite-sword damage, 6-block range,
 *                           slow mining (no bonus), tracks kills toward awakening.
 *   Tool Mode (toggle):     full mining speed (pickaxe + shovel), dirt → Core Dust,
 *                           Deep Reach on right-click.  Cannot deal real damage.
 *   At 100 mob kills:       awakens — permanent +15% damage buff.
 *   Deep Reach (Tool Mode): +4 extra blocks for 3 seconds, 30-second cooldown.
 *   Repair:                 Kay's Sharpening Stone only. No anvil ingredient.
 *   Cannot be crafted:      only found in Kay's Basement vault.
 *
 * CODE SECTIONS:
 *   1. Constants
 *   2. Constructor + attribute modifiers (damage / speed / reach)
 *   3. Mining  — isSuitableFor, getMiningSpeedMultiplier
 *   4. use()   — right-click: mode toggle, dust extraction, Deep Reach
 *   5. postHit() — after attack: block damage in Tool Mode, kill tracking, awakening
 *   6. inventoryTick() — expire Deep Reach timer each tick
 *   7. canRepairWith()
 *   8. Private helpers — Deep Reach activation/removal, NBT read/write
 */
public class Kays24InchItem extends Item {

    // =========================================================================
    // 1. CONSTANTS
    // =========================================================================

    // Unique Identifiers for every attribute modifier we create.
    // Minecraft needs these to find and remove modifiers later.
    // BASE_ATTACK_DAMAGE_MODIFIER_ID and BASE_ATTACK_SPEED_MODIFIER_ID are
    // inherited protected fields from the Item class (Vanilla IDs).
    private static final Identifier BLOCK_REACH_ID  = KayAndCarl.id("kays_24_inch_block_reach");
    private static final Identifier ENTITY_REACH_ID = KayAndCarl.id("kays_24_inch_entity_reach");
    private static final Identifier DEEP_BLOCK_ID   = KayAndCarl.id("kays_deep_reach_block");
    private static final Identifier DEEP_ENTITY_ID  = KayAndCarl.id("kays_deep_reach_entity");

    // Reach math explained:
    //   Survival block reach default = 4.5 blocks → we want 6.0 → ADD +1.5
    //   Survival entity reach default = 3.0 blocks → we want 6.0 → ADD +3.0
    //   Deep Reach target = 10 blocks → add another +4.0 on top of the above
    private static final double REACH_BLOCK_BONUS  = 1.5;
    private static final double REACH_ENTITY_BONUS = 3.0;
    private static final double DEEP_REACH_BONUS   = 4.0;

    private static final int DEEP_REACH_TICKS    = 60;  // 3 seconds (20 ticks = 1 sec)
    private static final int DEEP_REACH_COOLDOWN = 600; // 30 seconds

    // NBT keys — every piece of mutable state is saved here, inside the item itself.
    // This means the state survives chests, world saves, and server restarts.
    private static final String KEY_TOOL_MODE = "ToolMode";         // boolean
    private static final String KEY_KILLS     = "MobKills";         // int
    private static final String KEY_AWAKENED  = "Awakened";         // boolean
    private static final String KEY_BUFF      = "AwakenBuff";       // int (future choice UI)
    private static final String KEY_DR_END    = "DeepReachEndTick"; // long (world tick)
    private static final String KEY_DR_CD     = "DeepReachCDTick";  // long (world tick)

    // =========================================================================
    // 2. CONSTRUCTOR + ATTRIBUTE MODIFIERS
    // =========================================================================

    public Kays24InchItem(Settings settings) {
        super(settings);
    }

    /**
     * Returns the stat bonuses this item gives while held in the main hand.
     *
     * We set four modifiers:
     *   a) Attack damage  — 7.0 added to base 1.0 = 8 total (same as netherite sword)
     *   b) Attack speed   — -2.6 (slightly slower than netherite sword's -2.4, because
     *                        this tool does three jobs — it shouldn't swing fastest too)
     *   c) Block reach    — +1.5 → 4.5 + 1.5 = 6 blocks
     *   d) Entity reach   — +3.0 → 3.0 + 3.0 = 6 blocks
     *
     * AttributeModifierSlot.MAINHAND means these only apply in the main hand.
     *
     * NOTE: the interaction-range attributes were added in 1.20.5 and in 1.21.1
     * Yarn carry the PLAYER_ prefix (PLAYER_BLOCK_INTERACTION_RANGE /
     * PLAYER_ENTITY_INTERACTION_RANGE).
     */
    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 7.0, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.6, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE,
                        new EntityAttributeModifier(BLOCK_REACH_ID, REACH_BLOCK_BONUS, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                        new EntityAttributeModifier(ENTITY_REACH_ID, REACH_ENTITY_BONUS, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .build();
    }

    // =========================================================================
    // 3. MINING
    // =========================================================================

    /**
     * isCorrectForDrops — returns true if this item is the correct tool to make
     * the block drop its item (vs. destroying it without a drop). 1.21.1 renamed
     * the old isSuitableFor(BlockState) to isCorrectForDrops(ItemStack, BlockState).
     * We cover both pickaxe-mineable (stone, ores) and shovel-mineable (dirt, sand, gravel).
     */
    @Override
    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        return state.isIn(BlockTags.PICKAXE_MINEABLE)
                || state.isIn(BlockTags.SHOVEL_MINEABLE);
    }

    /**
     * getMiningSpeed — how fast each block is broken. (1.21.1 renamed the old
     * getMiningSpeedMultiplier to getMiningSpeed.)
     *
     * In Combat Mode: returns 1.0 (no bonus) — mining feels like bare hands.
     * In Tool Mode:   returns NETHERITE speed (9.0) for relevant blocks.
     *
     * This is the soft enforcement of "Combat Mode can't mine". The player can
     * technically break blocks in Combat Mode, but it takes an extremely long time.
     */
    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (!isToolMode(stack)) return 1.0f;

        if (state.isIn(BlockTags.PICKAXE_MINEABLE) || state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            return ToolMaterials.NETHERITE.getMiningSpeedMultiplier(); // 9.0
        }
        return 1.0f;
    }

    // =========================================================================
    // 4. USE (right-click)
    // =========================================================================

    /**
     * Called on right-click. Three behaviours depending on context:
     *
     *   A) Sneak + right-click → toggle Tool Mode / Combat Mode.
     *   B) Tool Mode, right-click aimed at dirt → extract Raw Core Dust (1–3).
     *   C) Tool Mode, right-click aimed elsewhere → activate Deep Reach (if off cooldown).
     *
     * success = "I handled this, stop processing."
     * pass    = "I did nothing, continue with default behaviour."
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // A) Mode toggle — sneak + right-click
        if (user.isSneaking()) {
            boolean newMode = !isToolMode(stack);
            setToolMode(stack, newMode);
            String label = newMode ? "§a[Tool Mode]" : "§c[Combat Mode]";
            user.sendMessage(Text.literal("Kay's 24 Inch → " + label), true); // true = action bar
            user.getItemCooldownManager().set(stack, 10); // brief cooldown to prevent spam
            return TypedActionResult.success(stack, world.isClient);
        }

        // B & C) Only available in Tool Mode
        if (isToolMode(stack)) {
            HitResult hit = user.raycast(6.0, 0, false); // raycast up to 6 blocks

            // B) Dirt-like block in aim — extract Core Dust
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                if (isDirtLike(world.getBlockState(pos))) {
                    if (!world.isClient) {
                        int amount = 1 + world.getRandom().nextInt(3);
                        for (int i = 0; i < amount; i++) {
                            user.getInventory().offerOrDrop(new ItemStack(ModItems.RAW_CORE_DUST));
                        }
                        user.sendMessage(Text.literal("Extracted §e" + amount + "x Raw Core Dust§r."), true);
                        stack.damage(1, user, EquipmentSlot.MAINHAND);
                    }
                    return TypedActionResult.success(stack, world.isClient);
                }
            }

            // C) No dirt aimed at — trigger Deep Reach (or report cooldown)
            if (!world.isClient) {
                long now = world.getTime();
                long cooldownEnd = getDeepReachCooldown(stack);
                if (now >= cooldownEnd) {
                    activateDeepReach(stack, user, now);
                } else {
                    long secsLeft = (cooldownEnd - now) / 20;
                    user.sendMessage(
                            Text.literal("§7Deep Reach: §c" + secsLeft + "s§7 cooldown remaining."), true);
                }
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        return TypedActionResult.pass(stack);
    }

    // =========================================================================
    // 5. POST-HIT (called after every melee strike)
    // =========================================================================

    /**
     * Called right after this item damages a LivingEntity.
     *
     * Two jobs here:
     *   a) Block real damage in Tool Mode — heal back most of the damage dealt.
     *      This is a placeholder. A Mixin on PlayerEntity.attack() is the clean
     *      solution; we add that in the Mixin step. For now this achieves the
     *      same result with minimal code.
     *
     *   b) Kill tracking — if the target died from this hit, increment the counter.
     *      At 100 kills the tool awakens.
     */
    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // a) Tool Mode: undo most of the damage (leaves 0.5 as feedback so the hit "feels" real)
        if (isToolMode(stack)) {
            if (attacker instanceof PlayerEntity player) {
                target.heal(6.5f);
                player.sendMessage(Text.literal("§7Switch to §c[Combat Mode]§7 to attack."), true);
            }
            return; // skip kill tracking when in Tool Mode
        }

        // b) Kill tracking (server-side only)
        if (!target.isAlive() && !attacker.getWorld().isClient) {
            int newKills = getKills(stack) + 1;
            setKills(stack, newKills);

            if (newKills == 50 && attacker instanceof PlayerEntity p) {
                p.sendMessage(Text.literal("§6[24 Inch] §750 kills... it's warming up."), false);
            }
            if (newKills >= 100 && !isAwakened(stack)) {
                triggerAwakening(stack, attacker);
            }
        }

        // c) Ember Coating — set target on fire for 4 seconds if coating is active
        if (!attacker.getWorld().isClient
                && EmberCoatingItem.isEmberActive(stack, attacker.getWorld().getTime())) {
            target.setOnFireFor(4);
        }

        // Consume one durability per hit (same as vanilla weapons)
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
    }

    // =========================================================================
    // 6. INVENTORY TICK (runs 20× per second while item is in the hotbar slot)
    // =========================================================================

    /**
     * Every tick, check whether the Deep Reach timer has expired. If it has,
     * remove the temporary reach boost from the player's attribute instances.
     *
     * We only process this when:
     *   • Server-side (world.isClient == false) — attributes are server-controlled
     *   • Item is selected (in the player's active hotbar slot)
     *   • Holder is a ServerPlayerEntity (mobs don't hold this weapon)
     */
    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity,
                               int slot, boolean selected) {
        if (world.isClient || !selected || !(entity instanceof ServerPlayerEntity player)) return;

        long now = world.getTime();
        long deepEnd = getDeepReachEnd(stack);

        if (deepEnd > 0 && now >= deepEnd) {
            removeDeepReach(player);
            setDeepReachEnd(stack, 0L);
            player.sendMessage(Text.literal("§7Deep Reach faded."), true);
        }
    }

    // =========================================================================
    // 7. REPAIR
    // =========================================================================

    /**
     * Limits anvil repair to Kay's Sharpening Stone only.
     * No netherite ingot, no other material. The stone also works in the field
     * (right-click logic handled in SharpeningStoneItem, built in a later step).
     */
    @Override
    public boolean canRepairWith(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(ModItems.SHARPENING_STONE);
    }

    // =========================================================================
    // 8. PRIVATE HELPERS
    // =========================================================================

    /**
     * Applies a temporary +4.0 reach boost on top of the item's permanent +1.5/+3.0.
     * Total reach during Deep Reach: 4.5 + 1.5 + 4.0 = 10.0 blocks.
     *
     * "Temporary" modifiers disappear on logout, but we persist the end tick in
     * NBT so inventoryTick() knows when to stop even if the modifier was re-added
     * on reconnect (or just cleans up gracefully).
     */
    private void activateDeepReach(ItemStack stack, PlayerEntity player, long now) {
        var blockAttr = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
        if (blockAttr != null && blockAttr.getModifier(DEEP_BLOCK_ID) == null) {
            blockAttr.addTemporaryModifier(
                    new EntityAttributeModifier(DEEP_BLOCK_ID, DEEP_REACH_BONUS, Operation.ADD_VALUE));
        }
        var entityAttr = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (entityAttr != null && entityAttr.getModifier(DEEP_ENTITY_ID) == null) {
            entityAttr.addTemporaryModifier(
                    new EntityAttributeModifier(DEEP_ENTITY_ID, DEEP_REACH_BONUS, Operation.ADD_VALUE));
        }

        setDeepReachEnd(stack, now + DEEP_REACH_TICKS);
        setDeepReachCooldown(stack, now + DEEP_REACH_COOLDOWN);
        player.sendMessage(Text.literal("§b[24 Inch] §fDeep Reach active for §b3 seconds§f!"), true);
    }

    private void removeDeepReach(ServerPlayerEntity player) {
        var b = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
        if (b != null) b.removeModifier(DEEP_BLOCK_ID);
        var e = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (e != null) e.removeModifier(DEEP_ENTITY_ID);
    }

    /**
     * Called at 100 kills. Currently auto-applies the damage buff and flags the item.
     * FUTURE: replaced by a choice screen (reach / damage / silk touch) once we
     * build the GUI layer. The NBT flag KEY_BUFF is already wired for that.
     */
    private void triggerAwakening(ItemStack stack, LivingEntity attacker) {
        setAwakened(stack, true);
        setBuff(stack, 1); // 1 = damage buff selected
        if (attacker instanceof PlayerEntity player) {
            player.sendMessage(Text.literal(
                    "\n§6✦ §eKay's 24 Inch has AWAKENED §6✦\n§7" +
                    "Damage permanently increased by §c15%§7.\n"), false);
        }
        // The +15% damage attribute is applied per-tick while awakened.
        // Implementation: ModDataAttachments (a later step). The flag is set here;
        // a tick event will read it and add/maintain the modifier on the player.
    }

    /** True for dirt, coarse dirt, rooted dirt, farmland, gravel, podzol — all extractable. */
    private boolean isDirtLike(BlockState state) {
        return state.isIn(BlockTags.DIRT)
                || state.isOf(Blocks.GRAVEL)
                || state.isOf(Blocks.FARMLAND)
                || state.isOf(Blocks.PODZOL);
    }

    // --- NBT helpers -----------------------------------------------------------
    // All item state lives in CUSTOM_DATA (a free-form NbtCompound attached to
    // the ItemStack). getData() copies it out, saveData() writes it back.

    private NbtCompound getData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private void saveData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public boolean isToolMode(ItemStack stack) {
        return getData(stack).getBoolean(KEY_TOOL_MODE);
        // Default on a fresh item: false = Combat Mode. The weapon is dangerous
        // until you actively choose to work with it.
    }

    private void setToolMode(ItemStack stack, boolean value) {
        NbtCompound nbt = getData(stack);
        nbt.putBoolean(KEY_TOOL_MODE, value);
        saveData(stack, nbt);
    }

    public int getKills(ItemStack stack) {
        return getData(stack).getInt(KEY_KILLS);
    }

    private void setKills(ItemStack stack, int value) {
        NbtCompound nbt = getData(stack);
        nbt.putInt(KEY_KILLS, value);
        saveData(stack, nbt);
    }

    private boolean isAwakened(ItemStack stack) {
        return getData(stack).getBoolean(KEY_AWAKENED);
    }

    private void setAwakened(ItemStack stack, boolean value) {
        NbtCompound nbt = getData(stack);
        nbt.putBoolean(KEY_AWAKENED, value);
        saveData(stack, nbt);
    }

    private void setBuff(ItemStack stack, int value) {
        NbtCompound nbt = getData(stack);
        nbt.putInt(KEY_BUFF, value);
        saveData(stack, nbt);
    }

    private long getDeepReachEnd(ItemStack stack) {
        return getData(stack).getLong(KEY_DR_END);
    }

    private void setDeepReachEnd(ItemStack stack, long tick) {
        NbtCompound nbt = getData(stack);
        nbt.putLong(KEY_DR_END, tick);
        saveData(stack, nbt);
    }

    private long getDeepReachCooldown(ItemStack stack) {
        return getData(stack).getLong(KEY_DR_CD);
    }

    private void setDeepReachCooldown(ItemStack stack, long tick) {
        NbtCompound nbt = getData(stack);
        nbt.putLong(KEY_DR_CD, tick);
        saveData(stack, nbt);
    }
}
