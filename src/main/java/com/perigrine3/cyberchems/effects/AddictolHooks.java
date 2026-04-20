package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictolHooks {

    private static final String NBT_ADDicted = "cc_addicted";

    @SubscribeEvent
    public static void onAddictolExpired(MobEffectEvent.Expired event) {
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null) return;
        if (!inst.is(ModEffects.ADDICTOL)) return;

        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) return;

        CompoundTag pd = living.getPersistentData();
        pd.putBoolean(NBT_ADDicted, false);

        if (living.hasEffect(ModEffects.ADDICTION)) {
            living.removeEffect(ModEffects.ADDICTION);
        }
    }
}