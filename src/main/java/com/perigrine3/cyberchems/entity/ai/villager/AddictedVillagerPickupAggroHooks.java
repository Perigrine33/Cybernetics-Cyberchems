package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictedVillagerPickupAggroHooks {

    private static final double AGGRO_RANGE_HORIZONTAL = 18.0D;
    private static final double AGGRO_RANGE_VERTICAL = 4.0D;

    private static final String NBT_HAS_THEFT_COMBAT_GOALS = "cc_has_theft_combat_goals";

    private AddictedVillagerPickupAggroHooks() {}

    @SubscribeEvent
    public static void onPlayerPickedUpItem(ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;

        ItemEntity pickedUpItemEntity = event.getItemEntity();
        if (!DrugItemUtil.isDrugItem(event.getOriginalStack())) return;

        UUID pickedUpItemUuid = pickedUpItemEntity.getUUID();

        AABB scanBox = player.getBoundingBox().inflate(AGGRO_RANGE_HORIZONTAL, AGGRO_RANGE_VERTICAL, AGGRO_RANGE_HORIZONTAL);
        List<Villager> nearbyVillagers = player.level().getEntitiesOfClass(Villager.class, scanBox, villager -> villager.hasEffect(ModEffects.ADDICTION)
                        && villager.getPersistentData().hasUUID(DrugRushPickupGoal.NBT_TARGET_ITEM_UUID));

        for (Villager villager : nearbyVillagers) {
            UUID villagerTargetUuid = villager.getPersistentData().getUUID(DrugRushPickupGoal.NBT_TARGET_ITEM_UUID);
            if (!pickedUpItemUuid.equals(villagerTargetUuid)) continue;

            ensureVillagerCanFight(villager);
            villager.setTarget(player);
        }
    }

    private static void ensureVillagerCanFight(Villager villager) {
        if (villager.getPersistentData().getBoolean(NBT_HAS_THEFT_COMBAT_GOALS)) return;
        villager.getPersistentData().putBoolean(NBT_HAS_THEFT_COMBAT_GOALS, true);

        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.20D, true));
        villager.targetSelector.addGoal(1, new HurtByTargetGoal(villager));
    }
}