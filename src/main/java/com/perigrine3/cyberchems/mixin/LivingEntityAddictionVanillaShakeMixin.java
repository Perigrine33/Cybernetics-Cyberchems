package com.perigrine3.cyberchems.mixin;

import com.mojang.math.Axis;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityAddictionVanillaShakeMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    private void cc$applyAddictionShake(
            LivingEntity livingEntity,
            float entityYaw,
            float partialTicks,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!livingEntity.hasEffect(ModEffects.ADDICTION)) {
            return;
        }

        float wobbleDegrees = (float) (Math.cos((double) livingEntity.tickCount * 3.25D) * Math.PI * 0.4D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-wobbleDegrees));
    }
}