package com.perigrine3.cyberchems.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class AddictolEffect extends MobEffect {

    public AddictolEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (living.level().isClientSide) return true;

        var inst = living.getEffect(ModEffects.ADDICTOL);
        if (inst == null) return true;

        int dur = inst.getDuration();
        if (dur <= 0) return true;

        if ((living.tickCount % 20) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur, 0, false, true, true));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, 0, false, true, true));
        }

        return true;
    }
}