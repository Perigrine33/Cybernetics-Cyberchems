package com.perigrine3.cyberchems.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class WarpEffect extends MobEffect {

    private static final String NBT_WARP_DONE = "cc_warp_done";
    private static final int RADIUS = 128;
    private static final int ATTEMPTS = 64;

    private static final int NAUSEA_TICKS = 200;

    public WarpEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof ServerPlayer player)) return true;
        if (!(player.level() instanceof ServerLevel level)) return true;

        CompoundTag pd = player.getPersistentData();
        if (pd.getBoolean(NBT_WARP_DONE)) return true;

        BlockPos origin = player.blockPosition();

        for (int i = 0; i < ATTEMPTS; i++) {
            int dx = player.getRandom().nextInt(RADIUS * 2 + 1) - RADIUS;
            int dz = player.getRandom().nextInt(RADIUS * 2 + 1) - RADIUS;
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight() + 1) continue;
            if (y >= level.getMaxBuildHeight() - 2) continue;

            BlockPos feet = new BlockPos(x, y, z);

            if (!level.getWorldBorder().isWithinBounds(feet)) continue;

            double tx = x + 0.5;
            double ty = y;
            double tz = z + 0.5;

            AABB movedBox = player.getBoundingBox().move(tx - player.getX(), ty - player.getY(), tz - player.getZ());
            if (!level.noCollision(player, movedBox)) continue;
            if (player.isPassenger()) player.stopRiding();

            float yaw = player.getYRot();
            float pitch = player.getXRot();

            player.teleportTo(level, tx, ty, tz, RelativeMovement.ALL, yaw, pitch);
            player.setDeltaMovement(0, 0, 0);
            player.hurtMarked = true;

            double dist2 = player.distanceToSqr(tx, ty, tz);
            if (dist2 > 16.0) continue;
            pd.putBoolean(NBT_WARP_DONE, true);

            int nausea = NAUSEA_TICKS + Mth.clamp(amplifier, 0, 10) * 40;
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, nausea, 0, false, true, true));

            return true;
        }

        pd.putBoolean(NBT_WARP_DONE, true);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS, 0, false, true, true));
        return true;
    }

    @Override
    public void onMobRemoved(LivingEntity living, int amplifier, Entity.RemovalReason reason) {
        if (living.level().isClientSide) return;
        living.getPersistentData().remove(NBT_WARP_DONE);
    }
}