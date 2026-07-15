package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class VillagerDrugBehaviorHooks {

    public static final String NBT_TARGET_ITEM_UUID =
            AddictedVillagerDrugRushGoal.NBT_TARGET_ITEM_UUID;

    private static final String NBT_ROID_RAGE_ACTIVE =
            "cc_roid_rage_active";

    private static final String NBT_LAST_RAGE_TARGET_TICK =
            "cc_last_rage_target_tick";

    private static final double RAGE_TARGET_RANGE =
            16.0D;

    private static final double RAGE_TARGET_VERTICAL_RANGE =
            6.0D;

    private static final int RAGE_TARGET_INTERVAL_TICKS =
            10;

    private static final Set<Villager> DRUG_GOAL_REGISTERED =
            Collections.newSetFromMap(
                    new WeakHashMap<>()
            );

    private static final Set<Villager> COMBAT_GOALS_REGISTERED =
            Collections.newSetFromMap(
                    new WeakHashMap<>()
            );

    private static final ResourceLocation ROID_RAGE_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(
                    Cyberchems.MODID,
                    "villager_roid_rage_speed"
            );

    private static final double ROID_RAGE_SPEED_BONUS =
            0.35D;

    private VillagerDrugBehaviorHooks() {}

    @SubscribeEvent
    public static void onVillagerJoin(
            EntityJoinLevelEvent event
    ) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (villager.level().isClientSide) {
            return;
        }

        villager.setCanPickUpLoot(true);

        if (!McaVillagerAiCompat.isMcaVillager(villager)) {
            ensureDrugRushGoal(villager);
        }
    }

    @SubscribeEvent
    public static void onVillagerTick(
            EntityTickEvent.Post event
    ) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (villager.level().isClientSide) {
            return;
        }

        boolean mcaVillager =
                McaVillagerAiCompat.isMcaVillager(villager);

        if (mcaVillager) {
            McaVillagerAiCompat.tickDrugRush(villager);
        } else {
            ensureDrugRushGoal(villager);
        }

        if (villager.hasEffect(ModEffects.ADDICTION)) {
            villager.setCanPickUpLoot(true);
        }

        handleRoidRage(
                villager,
                mcaVillager
        );
    }

    private static void ensureDrugRushGoal(
            Villager villager
    ) {
        if (!DRUG_GOAL_REGISTERED.add(villager)) {
            return;
        }

        villager.goalSelector.addGoal(
                0,
                new AddictedVillagerDrugRushGoal(villager)
        );
    }

    private static void handleRoidRage(
            Villager villager,
            boolean mcaVillager
    ) {
        MobEffectInstance roidInstance =
                villager.getEffect(ModEffects.ROID);

        CompoundTag persistentData =
                villager.getPersistentData();

        if (roidInstance == null) {
            persistentData.remove(
                    NBT_ROID_RAGE_ACTIVE
            );

            persistentData.remove(
                    NBT_LAST_RAGE_TARGET_TICK
            );

            removeRoidRageSpeed(villager);

            if (mcaVillager) {
                McaVillagerAiCompat.clearAttackTarget(
                        villager
                );
            } else {
                villager.setTarget(null);
            }

            return;
        }

        persistentData.putBoolean(
                NBT_ROID_RAGE_ACTIVE,
                true
        );

        applyRoidRageSpeed(villager);

        LivingEntity target =
                getOrFindRoidRageTarget(villager);

        if (mcaVillager) {
            McaVillagerAiCompat.tickRoidRage(
                    villager,
                    target
            );
        } else {
            ensureCombatGoals(villager);
            villager.setTarget(target);
        }
    }

    private static LivingEntity getOrFindRoidRageTarget(
            Villager villager
    ) {
        LivingEntity currentTarget =
                villager.getTarget();

        if (isValidRageTarget(
                villager,
                currentTarget
        )) {
            return currentTarget;
        }

        CompoundTag persistentData =
                villager.getPersistentData();

        long currentTick =
                villager.level().getGameTime();

        long lastTargetTick =
                persistentData.getLong(
                        NBT_LAST_RAGE_TARGET_TICK
                );

        if (currentTick - lastTargetTick
                < RAGE_TARGET_INTERVAL_TICKS) {
            return null;
        }

        LivingEntity nearestTarget =
                findNearestRageTarget(villager);

        persistentData.putLong(
                NBT_LAST_RAGE_TARGET_TICK,
                currentTick
        );

        return nearestTarget;
    }

    private static boolean isValidRageTarget(
            Villager villager,
            LivingEntity candidate
    ) {
        if (candidate == null) {
            return false;
        }

        if (candidate == villager) {
            return false;
        }

        if (!candidate.isAlive()) {
            return false;
        }

        if (candidate.isRemoved()) {
            return false;
        }

        if (candidate.isSpectator()) {
            return false;
        }

        if (candidate.level() != villager.level()) {
            return false;
        }

        return villager.distanceToSqr(candidate)
                <= RAGE_TARGET_RANGE
                * RAGE_TARGET_RANGE;
    }

    private static LivingEntity findNearestRageTarget(
            Villager villager
    ) {
        AABB searchBox =
                villager.getBoundingBox().inflate(
                        RAGE_TARGET_RANGE,
                        RAGE_TARGET_VERTICAL_RANGE,
                        RAGE_TARGET_RANGE
                );

        return villager.level().getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        candidate ->
                                isValidRageTarget(
                                        villager,
                                        candidate
                                )
                )
                .stream()
                .min(Comparator.comparingDouble(
                        villager::distanceToSqr
                ))
                .orElse(null);
    }

    private static void ensureCombatGoals(
            Villager villager
    ) {
        if (!COMBAT_GOALS_REGISTERED.add(villager)) {
            return;
        }

        villager.goalSelector.addGoal(
                1,
                new MeleeAttackGoal(
                        villager,
                        1.25D,
                        true
                )
        );

        villager.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(villager)
        );
    }

    private static void applyRoidRageSpeed(
            Villager villager
    ) {
        AttributeInstance movementSpeed =
                villager.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed == null) {
            return;
        }

        if (movementSpeed.getModifier(
                ROID_RAGE_SPEED_ID
        ) != null) {
            return;
        }

        movementSpeed.addPermanentModifier(
                new AttributeModifier(
                        ROID_RAGE_SPEED_ID,
                        ROID_RAGE_SPEED_BONUS,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
        );
    }

    private static void removeRoidRageSpeed(
            Villager villager
    ) {
        AttributeInstance movementSpeed =
                villager.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed == null) {
            return;
        }

        movementSpeed.removeModifier(
                ROID_RAGE_SPEED_ID
        );
    }
}