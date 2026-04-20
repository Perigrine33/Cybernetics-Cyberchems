package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class DrugAddictionRollHooks {

    private static final String NBT_ADDICTION_ROLL_GUARD_TICK = "cc_addiction_roll_guard_tick";
    private static final float ADDICTION_CHANCE = 0.10f;

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) return;
        MobEffectInstance added = event.getEffectInstance();

        boolean isDrug = added.is(ModEffects.ROID) || added.is(ModEffects.STIM) || added.is(ModEffects.BLACKLACE);

        if (!isDrug) return;

        long now = living.level().getGameTime();
        var pd = living.getPersistentData();
        if (pd.getLong(NBT_ADDICTION_ROLL_GUARD_TICK) == now) return;
        pd.putLong(NBT_ADDICTION_ROLL_GUARD_TICK, now);

        if (living.hasEffect(ModEffects.ADDICTION)) return;

        if (living.getRandom().nextFloat() < ADDICTION_CHANCE) {
            living.addEffect(new MobEffectInstance(ModEffects.ADDICTION, Integer.MAX_VALUE, 0, false, true, true));
        }
    }

    private DrugAddictionRollHooks() {}
}