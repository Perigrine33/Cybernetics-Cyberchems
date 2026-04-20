package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Cyberchems.MODID);

    public static final Holder<MobEffect> ROID = MOB_EFFECTS.register("roid",
            () -> new RoidEffect(MobEffectCategory.NEUTRAL, 0xFF1362));
    public static final Holder<MobEffect> STIM = MOB_EFFECTS.register("stim",
            () -> new StimEffect(MobEffectCategory.NEUTRAL, 0x21FFB5));
    public static final Holder<MobEffect> BLACKLACE = MOB_EFFECTS.register("blacklace",
            () -> new BlackLaceEffect(MobEffectCategory.NEUTRAL, 0x200101));
    public static final Holder<MobEffect> IMMUNOBOOST = MOB_EFFECTS.register("immunoboost",
            () -> new ImmunoboostEffect(MobEffectCategory.NEUTRAL, 0x6BB769));
    public static final Holder<MobEffect> WARP = MOB_EFFECTS.register("warp",
            () -> new WarpEffect(MobEffectCategory.NEUTRAL, 0xF913DC));
    public static final Holder<MobEffect> ADDICTOL = MOB_EFFECTS.register("addictol",
            () -> new AddictolEffect(MobEffectCategory.BENEFICIAL, 0x3321FF));

    public static final Holder<MobEffect> ADDICTION = MOB_EFFECTS.register("addiction",
            () -> new AddictionEffect(MobEffectCategory.HARMFUL, 0x000000));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }

    private ModEffects() {}
}