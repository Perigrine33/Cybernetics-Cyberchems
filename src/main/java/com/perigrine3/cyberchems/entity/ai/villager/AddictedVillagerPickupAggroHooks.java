package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictedVillagerPickupAggroHooks {

    private static final double AGGRO_RANGE_HORIZONTAL =
            18.0D;

    private static final double AGGRO_RANGE_VERTICAL =
            4.0D;

    private static final float MCA_CHASE_SPEED =
            1.20F;

    private static final Set<Villager> THEFT_COMBAT_GOALS_REGISTERED =
            Collections.newSetFromMap(
                    new WeakHashMap<>()
            );

    private AddictedVillagerPickupAggroHooks() {}

    @SubscribeEvent
    public static void onPlayerPickedUpItem(
            ItemEntityPickupEvent.Post event
    ) {
        Player player =
                event.getPlayer();

        if (player.level().isClientSide) {
            return;
        }

        if (!DrugItemUtil.isDrugItem(
                event.getOriginalStack()
        )) {
            return;
        }

        ItemEntity pickedUpItem =
                event.getItemEntity();

        UUID pickedUpItemUuid =
                pickedUpItem.getUUID();

        AABB scanBox =
                player.getBoundingBox().inflate(
                        AGGRO_RANGE_HORIZONTAL,
                        AGGRO_RANGE_VERTICAL,
                        AGGRO_RANGE_HORIZONTAL
                );

        List<Villager> nearbyVillagers =
                player.level().getEntitiesOfClass(
                        Villager.class,
                        scanBox,
                        villager ->
                                villager.hasEffect(
                                        ModEffects.ADDICTION
                                )
                                        && villager.getPersistentData()
                                        .hasUUID(
                                                VillagerDrugBehaviorHooks
                                                        .NBT_TARGET_ITEM_UUID
                                        )
                );

        for (Villager villager : nearbyVillagers) {
            UUID targetItemUuid =
                    villager.getPersistentData().getUUID(
                            VillagerDrugBehaviorHooks
                                    .NBT_TARGET_ITEM_UUID
                    );

            if (!pickedUpItemUuid.equals(targetItemUuid)) {
                continue;
            }

            villager.getPersistentData().remove(
                    VillagerDrugBehaviorHooks
                            .NBT_TARGET_ITEM_UUID
            );

            if (McaVillagerAiCompat.isMcaVillager(villager)) {
                aggroMcaVillager(
                        villager,
                        player
                );
            } else {
                aggroVanillaVillager(
                        villager,
                        player
                );
            }
        }
    }

    private static void aggroVanillaVillager(
            Villager villager,
            Player player
    ) {
        ensureVanillaCombatGoals(villager);
        villager.setTarget(player);
    }

    private static void ensureVanillaCombatGoals(
            Villager villager
    ) {
        if (!THEFT_COMBAT_GOALS_REGISTERED.add(villager)) {
            return;
        }

        villager.goalSelector.addGoal(
                1,
                new MeleeAttackGoal(
                        villager,
                        1.20D,
                        true
                )
        );

        villager.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(villager)
        );
    }

    private static void aggroMcaVillager(
            Villager villager,
            Player player
    ) {
        villager.setTarget(player);

        villager.getBrain().setMemory(
                MemoryModuleType.ATTACK_TARGET,
                player
        );

        villager.getBrain().setMemory(
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(
                        player,
                        true
                )
        );

        villager.getBrain().setMemory(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(
                        new EntityTracker(
                                player,
                                false
                        ),
                        MCA_CHASE_SPEED,
                        1
                )
        );
    }
}