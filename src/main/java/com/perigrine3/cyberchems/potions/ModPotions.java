package com.perigrine3.cyberchems.potions;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, Cyberchems.MODID);

    public static final Holder<Potion> ROID = POTIONS.register("roid_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.ROID, 3600, 0)));
    public static final Holder<Potion> STIM = POTIONS.register("stim_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.STIM, 3600, 0)));
    public static final Holder<Potion> BLACKLACE = POTIONS.register("blacklace_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.BLACKLACE, 3600, 0)));
    public static final Holder<Potion> IMMUNOBOOST = POTIONS.register("immunoboost_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.IMMUNOBOOST, 3600, 0)));
    public static final Holder<Potion> WARP = POTIONS.register("warp_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.WARP, 3600, 0)));
    public static final Holder<Potion> ADDICTOL = POTIONS.register("addictol_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.ADDICTOL, 3600, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}