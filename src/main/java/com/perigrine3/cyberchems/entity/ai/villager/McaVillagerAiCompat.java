package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.effects.AddictionEffect;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class McaVillagerAiCompat {

    private static final String MCA_VILLAGER_CLASS =
            "net.conczin.mca.entity.VillagerEntityMCA";

    public static final String NBT_TARGET_ITEM_UUID =
            "cc_addicted_target_drug_item_uuid";

    private static final String NBT_FLEE_UNTIL_TICK =
            "cc_addicted_flee_until_tick";

    private static final String NBT_LAST_DRUG_SEARCH_TICK =
            "cc_mca_last_drug_search_tick";

    private static final String NBT_LAST_ATTACK_TICK =
            "cc_mca_last_roid_attack_tick";

    private static final double DRUG_SEARCH_RANGE =
            18.0D;

    private static final double DRUG_SEARCH_VERTICAL_RANGE =
            4.0D;

    private static final double DRUG_PICKUP_DISTANCE_SQUARED =
            1.5D;

    private static final float DRUG_MOVE_SPEED =
            0.75F;

    private static final int DRUG_SEARCH_INTERVAL_TICKS =
            5;

    private static final int FLEE_DURATION_TICKS =
            80;

    private static final float FLEE_MOVE_SPEED =
            0.75F;

    private static final float RAGE_MOVE_SPEED =
            1.25F;

    private static final double RAGE_ATTACK_DISTANCE_SQUARED =
            4.0D;

    private static final int RAGE_ATTACK_COOLDOWN_TICKS =
            20;

    private McaVillagerAiCompat() {}

    public static boolean isMcaVillager(Villager villager) {
        if (villager == null) {
            return false;
        }

        Class<?> currentClass =
                villager.getClass();

        while (currentClass != null) {
            if (MCA_VILLAGER_CLASS.equals(currentClass.getName())) {
                return true;
            }

            currentClass =
                    currentClass.getSuperclass();
        }

        return false;
    }

    public static void tickDrugRush(Villager villager) {
        if (!isMcaVillager(villager)) {
            return;
        }

        if (!villager.hasEffect(ModEffects.ADDICTION)) {
            clearDrugTarget(villager);
            return;
        }

        if (isCurrentlyFleeing(villager)) {
            return;
        }

        long currentTick =
                villager.level().getGameTime();

        long lastSearchTick =
                villager.getPersistentData().getLong(
                        NBT_LAST_DRUG_SEARCH_TICK
                );

        ItemEntity targetDrug =
                findRememberedDrugTarget(villager);

        if (targetDrug == null
                || currentTick - lastSearchTick
                >= DRUG_SEARCH_INTERVAL_TICKS) {
            targetDrug =
                    findNearestDrug(villager);

            villager.getPersistentData().putLong(
                    NBT_LAST_DRUG_SEARCH_TICK,
                    currentTick
            );
        }

        if (targetDrug == null) {
            clearDrugTarget(villager);
            return;
        }

        villager.getPersistentData().putUUID(
                NBT_TARGET_ITEM_UUID,
                targetDrug.getUUID()
        );

        villager.setCanPickUpLoot(true);

        setEntityWalkTarget(
                villager,
                targetDrug,
                DRUG_MOVE_SPEED,
                0
        );

        villager.getNavigation().moveTo(
                targetDrug,
                DRUG_MOVE_SPEED
        );

        if (villager.distanceToSqr(targetDrug)
                <= DRUG_PICKUP_DISTANCE_SQUARED) {
            consumeDrug(
                    villager,
                    targetDrug
            );
        }
    }

    public static void tickRoidRage(
            Villager villager,
            LivingEntity target
    ) {
        if (!isMcaVillager(villager)) {
            return;
        }

        if (!isValidRageTarget(villager, target)) {
            clearAttackTarget(villager);
            return;
        }

        villager.setTarget(target);

        villager.getBrain().setMemory(
                MemoryModuleType.ATTACK_TARGET,
                target
        );

        setEntityWalkTarget(
                villager,
                target,
                RAGE_MOVE_SPEED,
                1
        );

        villager.getNavigation().moveTo(
                target,
                RAGE_MOVE_SPEED
        );

        villager.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        if (villager.distanceToSqr(target)
                > RAGE_ATTACK_DISTANCE_SQUARED) {
            return;
        }

        if (!villager.hasLineOfSight(target)) {
            return;
        }

        long currentTick =
                villager.level().getGameTime();

        long lastAttackTick =
                villager.getPersistentData().getLong(
                        NBT_LAST_ATTACK_TICK
                );

        if (currentTick - lastAttackTick
                < RAGE_ATTACK_COOLDOWN_TICKS) {
            return;
        }

        villager.swing(
                InteractionHand.MAIN_HAND
        );

        villager.doHurtTarget(target);

        villager.getPersistentData().putLong(
                NBT_LAST_ATTACK_TICK,
                currentTick
        );
    }

    public static void clearAttackTarget(
            Villager villager
    ) {
        if (!isMcaVillager(villager)) {
            return;
        }

        villager.setTarget(null);

        villager.getBrain().eraseMemory(
                MemoryModuleType.ATTACK_TARGET
        );

        villager.getPersistentData().remove(
                NBT_LAST_ATTACK_TICK
        );
    }

    private static boolean isValidRageTarget(
            Villager villager,
            LivingEntity target
    ) {
        return target != null
                && target != villager
                && target.isAlive()
                && !target.isRemoved()
                && !target.isSpectator()
                && target.level() == villager.level();
    }

    private static void setEntityWalkTarget(
            Villager villager,
            Entity target,
            float speed,
            int closeEnoughDistance
    ) {
        villager.getBrain().setMemory(
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(
                        target,
                        true
                )
        );

        villager.getBrain().setMemory(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(
                        new EntityTracker(
                                target,
                                false
                        ),
                        speed,
                        closeEnoughDistance
                )
        );
    }

    private static boolean isCurrentlyFleeing(
            Villager villager
    ) {
        return villager.tickCount
                < villager.getPersistentData().getInt(
                NBT_FLEE_UNTIL_TICK
        );
    }

    private static ItemEntity findRememberedDrugTarget(
            Villager villager
    ) {
        if (!villager.getPersistentData().hasUUID(
                NBT_TARGET_ITEM_UUID
        )) {
            return null;
        }

        var targetUuid =
                villager.getPersistentData().getUUID(
                        NBT_TARGET_ITEM_UUID
                );

        AABB searchBox =
                villager.getBoundingBox().inflate(
                        DRUG_SEARCH_RANGE,
                        DRUG_SEARCH_VERTICAL_RANGE,
                        DRUG_SEARCH_RANGE
                );

        List<ItemEntity> matches =
                villager.level().getEntitiesOfClass(
                        ItemEntity.class,
                        searchBox,
                        itemEntity ->
                                itemEntity.isAlive()
                                        && itemEntity.getUUID().equals(
                                        targetUuid
                                )
                                        && !itemEntity.getItem().isEmpty()
                                        && DrugItemUtil.isDrugItem(
                                        itemEntity.getItem()
                                )
                );

        return matches.isEmpty()
                ? null
                : matches.getFirst();
    }

    private static ItemEntity findNearestDrug(
            Villager villager
    ) {
        AABB searchBox =
                villager.getBoundingBox().inflate(
                        DRUG_SEARCH_RANGE,
                        DRUG_SEARCH_VERTICAL_RANGE,
                        DRUG_SEARCH_RANGE
                );

        List<ItemEntity> drugItems =
                villager.level().getEntitiesOfClass(
                        ItemEntity.class,
                        searchBox,
                        itemEntity ->
                                itemEntity.isAlive()
                                        && !itemEntity.getItem().isEmpty()
                                        && DrugItemUtil.isDrugItem(
                                        itemEntity.getItem()
                                )
                );

        return drugItems.stream()
                .min(Comparator.comparingDouble(
                        villager::distanceToSqr
                ))
                .orElse(null);
    }

    private static void consumeDrug(
            Villager villager,
            ItemEntity itemEntity
    ) {
        ItemStack groundStack =
                itemEntity.getItem();

        if (groundStack.isEmpty()
                || !DrugItemUtil.isDrugItem(groundStack)) {
            clearDrugTarget(villager);
            return;
        }

        Vec3 pickupPosition =
                itemEntity.position();

        ItemStack consumedStack =
                groundStack.split(1);

        if (groundStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(
                    groundStack
            );
        }

        applyDrugEffect(
                villager,
                consumedStack
        );

        leaveContainer(
                villager,
                consumedStack
        );

        beginFleeing(
                villager,
                pickupPosition
        );

        clearDrugTarget(villager);
    }

    private static void applyDrugEffect(
            Villager villager,
            ItemStack stack
    ) {
        Holder<MobEffect> effect =
                getDrugEffect(stack);

        if (effect == null) {
            return;
        }

        MobEffectInstance existing =
                villager.getEffect(effect);

        int duration =
                existing == null
                        ? AddictionEffect.DURATION_7_DAYS_TICKS
                        : Math.max(
                        existing.getDuration(),
                        AddictionEffect.DURATION_7_DAYS_TICKS
                );

        int amplifier =
                existing == null
                        ? 0
                        : existing.getAmplifier();

        villager.addEffect(new MobEffectInstance(
                effect,
                duration,
                amplifier,
                false,
                true,
                true
        ));
    }

    private static Holder<MobEffect> getDrugEffect(
            ItemStack stack
    ) {
        if (DrugItemUtil.isRoidItem(stack)) {
            return ModEffects.ROID;
        }

        if (DrugItemUtil.isStimItem(stack)) {
            return ModEffects.STIM;
        }

        if (DrugItemUtil.isBlacklaceItem(stack)) {
            return ModEffects.BLACKLACE;
        }

        return null;
    }

    private static void leaveContainer(
            Villager villager,
            ItemStack consumedStack
    ) {
        if (DrugItemUtil.isDrugAutoinjector(
                consumedStack
        )) {
            villager.spawnAtLocation(
                    new ItemStack(
                            ModItems.EMPTY_AUTOINJECTOR.get()
                    )
            );

            return;
        }

        if (DrugItemUtil.isRegularPotion(
                consumedStack
        )) {
            villager.spawnAtLocation(
                    new ItemStack(
                            Items.GLASS_BOTTLE
                    )
            );
        }
    }

    private static void beginFleeing(
            Villager villager,
            Vec3 sourcePosition
    ) {
        villager.getPersistentData().putInt(
                NBT_FLEE_UNTIL_TICK,
                villager.tickCount + FLEE_DURATION_TICKS
        );

        Vec3 away =
                villager.position()
                        .subtract(sourcePosition);

        Vec3 horizontalAway =
                new Vec3(
                        away.x,
                        0.0D,
                        away.z
                );

        BlockPos targetPosition;

        if (horizontalAway.lengthSqr() > 0.0001D) {
            Vec3 direction =
                    horizontalAway.normalize()
                            .scale(10.0D);

            targetPosition =
                    BlockPos.containing(
                            villager.getX() + direction.x,
                            villager.getY(),
                            villager.getZ() + direction.z
                    );
        } else {
            int offsetX =
                    villager.getRandom().nextInt(17) - 8;

            int offsetZ =
                    villager.getRandom().nextInt(17) - 8;

            targetPosition =
                    villager.blockPosition().offset(
                            offsetX,
                            0,
                            offsetZ
                    );
        }

        villager.getBrain().setMemory(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(
                        targetPosition,
                        FLEE_MOVE_SPEED,
                        1
                )
        );
    }

    private static void clearDrugTarget(
            Villager villager
    ) {
        villager.getPersistentData().remove(
                NBT_TARGET_ITEM_UUID
        );

        villager.getPersistentData().remove(
                NBT_LAST_DRUG_SEARCH_TICK
        );
    }
}