package com.groomapack.entity;

import com.groomapack.registry.ModEntityTypes;
import com.groomapack.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/**
 * The Core Grenade projectile.
 *
 * Thrown on right-click by CoreGrenadeItem. On any collision (block or entity),
 * it applies a shockwave:
 *   • 5-block radius knockback on all LivingEntities
 *   • Entities are pushed OUTWARD from the impact point + slightly upward
 *   • Zero block damage, zero direct damage — pure crowd-control
 *
 * The throw arc is identical to a snowball (light, fast, slight gravity).
 */
public class CoreGrenadeEntity extends ThrownItemEntity {

    public CoreGrenadeEntity(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    public CoreGrenadeEntity(World world, LivingEntity thrower) {
        super(ModEntityTypes.CORE_GRENADE, thrower, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CORE_GRENADE;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            explode();
        }
    }

    private void explode() {
        World world = this.getWorld();
        double cx = this.getX(), cy = this.getY(), cz = this.getZ();

        // Push all living entities in 5-block radius outward.
        world.getEntitiesByClass(LivingEntity.class,
                this.getBoundingBox().expand(5.0),
                e -> !(e instanceof PlayerEntity p && p.isCreative()))
             .forEach(target -> {
                 double dx = target.getX() - cx;
                 double dz = target.getZ() - cz;
                 double dist = Math.sqrt(dx * dx + dz * dz);
                 if (dist == 0) dist = 0.01;
                 double strength = 1.8 * (1.0 - Math.min(dist / 5.0, 1.0));
                 target.addVelocity(
                         (dx / dist) * strength,
                         0.4 * strength,
                         (dz / dist) * strength);
                 target.velocityDirty = true;
             });

        // Visual + sound — server sends particles to all nearby clients.
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.EXPLOSION, cx, cy, cz, 3, 0.5, 0.5, 0.5, 0.1);
            sw.spawnParticles(ParticleTypes.POOF,      cx, cy, cz, 12, 1.0, 1.0, 1.0, 0.3);
        }
        world.playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 0.6f, 1.4f);

        this.discard();
    }
}
