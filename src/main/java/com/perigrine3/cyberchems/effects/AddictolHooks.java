package com.perigrine3.cyberchems.effects;

import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictolHooks {

    private AddictolHooks() {}

    @SubscribeEvent
    public static void onAddictolExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance =
                event.getEffectInstance();

        if (instance == null
                || !instance.is(ModEffects.ADDICTOL)) {
            return;
        }

        LivingEntity living = event.getEntity();

        if (living.level().isClientSide) {
            return;
        }

        CompoundTag persistentData =
                living.getPersistentData();

        persistentData.putBoolean(
                AddictionHooks.NBT_ADDICTED,
                false
        );

        if (living.hasEffect(ModEffects.ADDICTION)) {
            living.removeEffect(ModEffects.ADDICTION);
        }

        if (living instanceof Player player) {
            AddictionHooks.clearAddictionPenalty(player);
        }
    }
}