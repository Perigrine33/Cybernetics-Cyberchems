package com.perigrine3.cyberchems.effects;

import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AddictionEffect extends MobEffect {

    public static final int DURATION_7_DAYS_TICKS = 7 * 24000;

    private static final ResourceLocation ID_SLOW = ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "addiction_slow");
    private static final ResourceLocation ID_WEAK = ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "addiction_weak");

    public AddictionEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ID_SLOW, -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ID_WEAK, -4.0D, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}