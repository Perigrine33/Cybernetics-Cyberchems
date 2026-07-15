package com.perigrine3.cyberchems.item.cyberdrugs;

import com.perigrine3.createcybernetics.item.generic.BaseAutoinjectorItem;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class StimAutoinjector extends BaseAutoinjectorItem {

    private static final int EFFECT_DURATION = 24000;
    private static final int EFFECT_AMPLIFIER = 0;

    public StimAutoinjector(Properties properties) {
        super(properties);
    }

    protected Optional<String> getDurationTranslationKey() {
        return Optional.of("item.cyberchems.stim_autoinjector.duration");
    }

    protected Optional<String> getDescriptionTranslationKey() {
        return Optional.of("item.cyberchems.stim_autoinjector.desc");
    }

    @Override
    public List<MobEffectInstance> getSpinalInjectionEffects(ItemStack stack) {
        return List.of(new MobEffectInstance(ModEffects.STIM, EFFECT_DURATION, EFFECT_AMPLIFIER));
    }
}