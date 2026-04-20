package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictedVillagerProfessionHooks {

    private AddictedVillagerProfessionHooks() {}

    @SubscribeEvent
    public static void onVillagerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;
        if (!villager.hasEffect(ModEffects.ADDICTION)) return;

        VillagerData villagerData = villager.getVillagerData();
        if (villagerData.getProfession() != VillagerProfession.NITWIT) {
            villager.setVillagerData(new VillagerData(villagerData.getType(), VillagerProfession.NITWIT, 1));
            villager.setVillagerXp(0);
        }

        villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
        villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);

        villager.setCanPickUpLoot(true);
    }
}