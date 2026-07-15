package com.perigrine3.cyberchems.item.cyberdrugs;

import com.perigrine3.createcybernetics.item.generic.BaseAutoinjectorItem;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class BlackLaceAutoinjector extends BaseAutoinjectorItem {

    private static final int EFFECT_DURATION = 24000;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final int MAX_EFFECT_AMPLIFIER = 3;

    public BlackLaceAutoinjector(Properties properties) {
        super(properties);
    }

    protected Optional<String> getDurationTranslationKey() {
        return Optional.of("item.cyberchems.blacklace_autoinjector.duration");
    }

    protected Optional<String> getDescriptionTranslationKey() {
        return Optional.of("item.cyberchems.blacklace_autoinjector.desc");
    }

    @Override
    public List<MobEffectInstance> getSpinalInjectionEffects(ItemStack stack) {
        return List.of(new MobEffectInstance(ModEffects.BLACKLACE, EFFECT_DURATION, EFFECT_AMPLIFIER));
    }

    @Override
    protected void applyInjection(Player user, LivingEntity target, ItemStack stack) {
        applyDose(target);
    }

    @Override
    public boolean shouldSpinalInjectorInject(ServerPlayer player, ItemStack stack) {
        MobEffectInstance current = player.getEffect(ModEffects.BLACKLACE);
        return current == null || current.getAmplifier() < MAX_EFFECT_AMPLIFIER;
    }

    private static int getNextAmplifier(LivingEntity target) {
        MobEffectInstance current = target.getEffect(ModEffects.BLACKLACE);
        if (current == null) {
            return EFFECT_AMPLIFIER;
        }

        return Math.min(MAX_EFFECT_AMPLIFIER, current.getAmplifier() + 1);
    }

    private static void applyDose(LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                ModEffects.BLACKLACE,
                EFFECT_DURATION,
                getNextAmplifier(target)
        ));
    }
}