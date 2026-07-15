package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.effects.AddictionEffect;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public final class AddictedVillagerDrugRushGoal extends Goal {

    public static final String NBT_TARGET_ITEM_UUID =
            "cc_addicted_target_drug_item_uuid";

    public static final String NBT_FLEE_UNTIL_TICK =
            "cc_addicted_flee_until_tick";

    private static final double SEARCH_RANGE_HORIZONTAL =
            18.0D;

    private static final double SEARCH_RANGE_VERTICAL =
            4.0D;

    private static final double MOVE_SPEED =
            0.75D;

    private static final double PICKUP_DISTANCE_SQUARED =
            1.5D;

    private static final int SEARCH_INTERVAL_TICKS =
            10;

    private static final int REPATH_INTERVAL_TICKS =
            5;

    private static final int FLEE_DURATION_TICKS =
            80;

    private static final double FLEE_SPEED =
            0.75D;

    private final Villager villager;

    private ItemEntity targetDrug;

    private int searchCooldown;
    private int repathCooldown;

    public AddictedVillagerDrugRushGoal(Villager villager) {
        this.villager = villager;

        setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (!villager.hasEffect(ModEffects.ADDICTION)) {
            clearTargetUuid();
            return false;
        }

        if (isCurrentlyFleeing()) {
            clearTargetUuid();
            return false;
        }

        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }

        searchCooldown =
                SEARCH_INTERVAL_TICKS;

        targetDrug =
                findNearestDrug();

        return isValidTarget(targetDrug);
    }

    @Override
    public boolean canContinueToUse() {
        return villager.hasEffect(ModEffects.ADDICTION)
                && !isCurrentlyFleeing()
                && isValidTarget(targetDrug);
    }

    @Override
    public void start() {
        repathCooldown = 0;

        updateTargetUuid();
        moveTowardTarget();
    }

    @Override
    public void tick() {
        if (!isValidTarget(targetDrug)) {
            return;
        }

        updateTargetUuid();

        villager.getLookControl().setLookAt(
                targetDrug,
                30.0F,
                30.0F
        );

        if (repathCooldown > 0) {
            repathCooldown--;
        }

        if (repathCooldown <= 0
                || villager.getNavigation().isDone()) {
            repathCooldown =
                    REPATH_INTERVAL_TICKS;

            moveTowardTarget();
        }

        if (villager.distanceToSqr(targetDrug)
                <= PICKUP_DISTANCE_SQUARED) {
            consumeTargetDrug();
        }
    }

    @Override
    public void stop() {
        clearTargetUuid();

        if (!isCurrentlyFleeing()) {
            villager.getNavigation().stop();
        }

        targetDrug = null;
        repathCooldown = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isCurrentlyFleeing() {
        return villager.tickCount
                < villager.getPersistentData().getInt(
                NBT_FLEE_UNTIL_TICK
        );
    }

    private boolean isValidTarget(ItemEntity itemEntity) {
        if (itemEntity == null) {
            return false;
        }

        if (!itemEntity.isAlive()) {
            return false;
        }

        if (itemEntity.getItem().isEmpty()) {
            return false;
        }

        if (!DrugItemUtil.isDrugItem(itemEntity.getItem())) {
            return false;
        }

        double maximumDistanceSquared =
                SEARCH_RANGE_HORIZONTAL
                        * SEARCH_RANGE_HORIZONTAL;

        return villager.distanceToSqr(itemEntity)
                <= maximumDistanceSquared;
    }

    private void updateTargetUuid() {
        if (targetDrug == null) {
            return;
        }

        villager.getPersistentData().putUUID(
                NBT_TARGET_ITEM_UUID,
                targetDrug.getUUID()
        );
    }

    private void clearTargetUuid() {
        villager.getPersistentData().remove(
                NBT_TARGET_ITEM_UUID
        );
    }

    private void moveTowardTarget() {
        if (!isValidTarget(targetDrug)) {
            return;
        }

        villager.getNavigation().moveTo(
                targetDrug,
                MOVE_SPEED
        );
    }

    private ItemEntity findNearestDrug() {
        AABB searchBox =
                villager.getBoundingBox().inflate(
                        SEARCH_RANGE_HORIZONTAL,
                        SEARCH_RANGE_VERTICAL,
                        SEARCH_RANGE_HORIZONTAL
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

    private void consumeTargetDrug() {
        if (!isValidTarget(targetDrug)) {
            return;
        }

        ItemStack groundStack =
                targetDrug.getItem();

        Vec3 pickupPosition =
                targetDrug.position();

        ItemStack consumedStack =
                groundStack.split(1);

        if (groundStack.isEmpty()) {
            targetDrug.discard();
        } else {
            targetDrug.setItem(groundStack);
        }

        applyDrugEffect(consumedStack);
        leaveContainer(consumedStack);
        beginFleeing(pickupPosition);

        clearTargetUuid();

        targetDrug = null;
    }

    private void applyDrugEffect(ItemStack stack) {
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

    private Holder<MobEffect> getDrugEffect(ItemStack stack) {
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

    private void leaveContainer(ItemStack consumedStack) {
        if (DrugItemUtil.isDrugAutoinjector(consumedStack)) {
            villager.spawnAtLocation(
                    new ItemStack(
                            ModItems.EMPTY_AUTOINJECTOR.get()
                    )
            );

            return;
        }

        if (DrugItemUtil.isRegularPotion(consumedStack)) {
            villager.spawnAtLocation(
                    new ItemStack(
                            Items.GLASS_BOTTLE
                    )
            );
        }
    }

    private void beginFleeing(Vec3 sourcePosition) {
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

        villager.getNavigation().moveTo(
                targetPosition.getX(),
                targetPosition.getY(),
                targetPosition.getZ(),
                FLEE_SPEED
        );
    }
}