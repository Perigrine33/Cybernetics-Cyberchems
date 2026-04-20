package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BlackLaceHooks {

    private static final String NBT_CRASH_REMAINING = "cc_blacklace_crash_remaining";
    private static final int CRASH_DURATION_TICKS = 20 * 60 * 5;
    private static final int ROLL_INTERVAL_TICKS = 40; // every 2 seconds
    private static final float WITHER_ROLL_CHANCE = 0.12f;
    private static final int WITHER_BOUT_DURATION = 60; // 3 seconds
    private static final int WITHER_BOUT_AMP = 0;
    private static final float DR_BASE = 0.40f;
    private static final float DR_PER_AMP = 0.15f;
    private static final float DR_CAP = 0.80f;
    private static final int AMP_CAP = 3;

    private BlackLaceHooks() {}

    private static MobEffectInstance getBlackLace(LivingEntity e) {
        return e.getEffect(ModEffects.BLACKLACE);
    }

    private static boolean hasBlackLace(LivingEntity e) {
        return getBlackLace(e) != null;
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (!hasBlackLace(entity)) return;

        MobEffectInstance inst = event.getEffectInstance();
        if (inst != null && inst.is(MobEffects.WITHER)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) return;

        MobEffectInstance bl = getBlackLace(living);
        if (bl == null) return;

        int amp = bl.getAmplifier();
        if (amp > AMP_CAP) amp = AMP_CAP;

        float reduction = DR_BASE + (DR_PER_AMP * amp);
        if (reduction > DR_CAP) reduction = DR_CAP;

        event.setNewDamage(event.getNewDamage() * (1.0f - reduction));
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null || !inst.is(ModEffects.BLACKLACE)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        startCrash(player);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null || !inst.is(ModEffects.BLACKLACE)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        startCrash(player);
    }

    private static void startCrash(Player player) {
        CompoundTag pd = player.getPersistentData();
        int remaining = pd.getInt(NBT_CRASH_REMAINING);
        pd.putInt(NBT_CRASH_REMAINING, Math.max(remaining, CRASH_DURATION_TICKS));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        CompoundTag pd = player.getPersistentData();
        int remaining = pd.getInt(NBT_CRASH_REMAINING);
        if (remaining <= 0) return;

        pd.putInt(NBT_CRASH_REMAINING, remaining - 1);

        if (hasBlackLace(player)) return;

        if ((player.tickCount % ROLL_INTERVAL_TICKS) != 0) return;
        if (player.getRandom().nextFloat() >= WITHER_ROLL_CHANCE) return;

        MobEffectInstance cur = player.getEffect(MobEffects.WITHER);
        if (cur != null && cur.getDuration() > WITHER_BOUT_DURATION / 2) return;

        player.addEffect(new MobEffectInstance(
                MobEffects.WITHER,
                WITHER_BOUT_DURATION,
                WITHER_BOUT_AMP,
                false,
                true,
                true
        ));
    }
}