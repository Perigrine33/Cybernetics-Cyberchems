package com.perigrine3.cyberchems.item.cyberdrugs;

import com.perigrine3.createcybernetics.item.generic.BaseAutoinjectorItem;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class AddictolAutoinjector extends BaseAutoinjectorItem {

    private static final int EFFECT_DURATION = 12000;
    private static final int EFFECT_AMPLIFIER = 0;

    public AddictolAutoinjector(Properties properties) {
        super(properties);
    }

    protected Optional<String> getDurationTranslationKey() {
        return Optional.of("item.cyberchems.addictol_autoinjector.duration");
    }

    protected Optional<String> getDescriptionTranslationKey() {
        return Optional.of("item.cyberchems.addictol_autoinjector.desc");
    }

    @Override
    public List<MobEffectInstance> getSpinalInjectionEffects(ItemStack stack) {
        return List.of(new MobEffectInstance(ModEffects.ADDICTOL, EFFECT_DURATION, EFFECT_AMPLIFIER));
    }
}