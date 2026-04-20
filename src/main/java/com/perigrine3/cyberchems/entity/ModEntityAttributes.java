package com.perigrine3.cyberchems.entity;

import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = Cyberchems.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {

    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.VILLAGER, Attributes.ATTACK_DAMAGE)) {
            event.add(EntityType.VILLAGER, Attributes.ATTACK_DAMAGE, 2.0D);
        }
    }
}