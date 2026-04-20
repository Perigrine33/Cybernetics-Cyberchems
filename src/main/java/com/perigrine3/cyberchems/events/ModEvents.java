package com.perigrine3.cyberchems.events;

import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.potions.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = Cyberchems.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    @SubscribeEvent
    public static void onBrewingRecipeRegister(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.THICK,   ModItems.WETWARE_RAVAGERTENDONS.get(), ModPotions.ROID);
        builder.addMix(Potions.THICK,   Items.   REDSTONE,                     ModPotions.STIM);
        builder.addMix(Potions.THICK,   Items.   WITHER_SKELETON_SKULL,        ModPotions.BLACKLACE);
        builder.addMix(Potions.THICK,   Items.   GLISTERING_MELON_SLICE,       ModPotions.IMMUNOBOOST);
        builder.addMix(Potions.THICK,   Items.   ENDER_PEARL,                  ModPotions.WARP);
        builder.addMix(Potions.MUNDANE, Items.   GHAST_TEAR,                   ModPotions.ADDICTOL);
    }
}
