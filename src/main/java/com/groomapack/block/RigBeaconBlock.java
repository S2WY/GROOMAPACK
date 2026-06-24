package com.groomapack.block;

import com.groomapack.registry.ModEntityTypes;
import com.groomapack.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Rig Beacon — summons The Rig mech when activated with Carl's Core.
 *
 * Mechanic:
 *   Place the Rig Beacon on a flat surface with at least 3 blocks of vertical
 *   clearance. Right-click while holding Carl's Core. The Beacon charges
 *   (sound + particles for 1 tick), then spawns The Rig entity directly above.
 *   Carl's Core is consumed. One Beacon can only summon one Rig at a time —
 *   it checks for an existing TheRigEntity within 8 blocks and refuses if found.
 *
 * After summoning: the beacon itself remains as an anchor/decoration. Destroying
 * it does not despawn The Rig (Carl's already in there).
 */
public class RigBeaconBlock extends Block {

    public RigBeaconBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        var held = player.getMainHandStack();
        if (!held.isOf(ModItems.CARL_CORE)) {
            player.sendMessage(
                    Text.literal("§7[Rig Beacon] Hold Carl's Core to activate."), true);
            return ActionResult.PASS;
        }

        // Check clearance — need 4 air blocks above.
        for (int dy = 1; dy <= 4; dy++) {
            if (!world.getBlockState(pos.up(dy)).isAir()) {
                player.sendMessage(
                        Text.literal("§c[Rig Beacon] Not enough clearance above (need 4 blocks)."),
                        true);
                return ActionResult.FAIL;
            }
        }

        // Spawn The Rig two blocks above the beacon.
        var rig = ModEntityTypes.THE_RIG.create(world);
        if (rig == null) return ActionResult.FAIL;

        rig.refreshPositionAndAngles(
                pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5,
                player.getYaw(), 0);
        world.spawnEntity(rig);

        if (!player.getAbilities().creativeMode) held.decrement(1);

        world.playSound(null, pos,
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 0.8f);
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    30, 0.5, 1.0, 0.5, 0.2);
        }
        player.sendMessage(Text.literal("§6The Rig is assembled. §fCarl is ready."), false);

        return ActionResult.SUCCESS;
    }
}
