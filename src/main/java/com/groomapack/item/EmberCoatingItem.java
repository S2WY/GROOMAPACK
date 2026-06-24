package com.groomapack.item;

import com.groomapack.registry.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Ember Coating — applies an ember charge to Kay's 24 Inch.
 *
 * Hold Ember Coating + Kay's 24 Inch (other hand), right-click. The tool
 * gains an "ember-coated" flag in its NBT for 5 minutes (6000 ticks). While
 * active, Kays24InchItem.postHit() checks this flag and sets the target on
 * fire for 4 seconds on every hit.
 *
 * Stack size 1. Crafted: 2 Ember Dust + Blaze Powder + Paper.
 */
public class EmberCoatingItem extends Item {

    public static final String KEY_EMBER   = "EmberCoated";
    public static final String KEY_EMBER_T = "EmberEndTick";
    private static final int DURATION_TICKS = 6000; // 5 minutes

    public EmberCoatingItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack coating = user.getStackInHand(hand);
        Hand otherHand = (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack target = user.getStackInHand(otherHand);

        if (target.isOf(ModItems.KAYS_24_INCH)) {
            if (!world.isClient) {
                NbtCompound nbt = getNbt(target);
                nbt.putBoolean(KEY_EMBER, true);
                nbt.putLong(KEY_EMBER_T, world.getTime() + DURATION_TICKS);
                saveNbt(target, nbt);

                if (!user.getAbilities().creativeMode) coating.decrement(1);

                user.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.PLAYERS, 1.0f, 0.7f);
                user.sendMessage(
                        Text.literal("§6[24 Inch] §fEmber Coating active for §65 minutes§f."),
                        true);
            }
            return TypedActionResult.success(coating, world.isClient);
        }

        if (!world.isClient) {
            user.sendMessage(Text.literal("§7Ember Coating only works on Kay's 24 Inch."), true);
        }
        return TypedActionResult.pass(coating);
    }

    // --- shared NBT helpers (used by Kays24InchItem too) ---

    private static NbtCompound getNbt(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static void saveNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /** Called by Kays24InchItem.postHit() to check if the coating is still active. */
    public static boolean isEmberActive(ItemStack stack, long worldTime) {
        NbtCompound nbt = getNbt(stack);
        return nbt.getBoolean(KEY_EMBER) && worldTime < nbt.getLong(KEY_EMBER_T);
    }
}
