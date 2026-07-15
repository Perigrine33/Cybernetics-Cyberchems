package com.perigrine3.cyberchems.item.cyberdrugs;

import com.perigrine3.createcybernetics.item.generic.BaseAutoinjectorItem;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class WarpAutoinjector extends BaseAutoinjectorItem {

    private static final int EFFECT_DURATION = 20;
    private static final int EFFECT_AMPLIFIER = 0;

    public WarpAutoinjector(Properties properties) {
        super(properties);
    }

    protected Optional<String> getDurationTranslationKey() {
        return Optional.of("item.cyberchems.warp_autoinjector.duration");
    }

    protected Optional<String> getDescriptionTranslationKey() {
        return Optional.empty();
    }

    @Override
    public List<MobEffectInstance> getSpinalInjectionEffects(ItemStack stack) {
        return List.of(new MobEffectInstance(ModEffects.WARP, EFFECT_DURATION, EFFECT_AMPLIFIER));
    }
}