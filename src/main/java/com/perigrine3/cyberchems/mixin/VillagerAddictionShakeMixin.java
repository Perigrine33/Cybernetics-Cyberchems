package com.perigrine3.cyberchems.mixin;

import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class VillagerAddictionShakeMixin {

    @ModifyVariable(
            method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private float cyberchems$applyColdPiglinStyleShakeToYaw(
            float rotationYaw,
            LivingEntity entity
    ) {
        if (entity instanceof AbstractVillager && entity.hasEffect(ModEffects.ADDICTION)) {
            rotationYaw += (float) (Math.cos((double) entity.tickCount * 3.25D) * Math.PI * 0.4D);
        }

        return rotationYaw;
    }
}