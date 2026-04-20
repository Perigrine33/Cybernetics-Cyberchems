package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DrugRushPickupGoal extends Goal {

    public static final String NBT_TARGET_ITEM_UUID = "cc_addicted_target_drug_item_uuid";

    private final Villager villager;
    private final double moveSpeed;
    private final double searchRange;

    private ItemEntity targetItemEntity;

    public DrugRushPickupGoal(Villager villager, double moveSpeed, double searchRange) {
        this.villager = villager;
        this.moveSpeed = moveSpeed;
        this.searchRange = searchRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (villager.level().isClientSide) return false;
        if (!villager.hasEffect(ModEffects.ADDICTION)) return false;

        targetItemEntity = findNearestDrugItem();
        return targetItemEntity != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (villager.level().isClientSide) return false;
        if (!villager.hasEffect(ModEffects.ADDICTION)) return false;
        if (targetItemEntity == null || !targetItemEntity.isAlive()) return false;

        double maxDistanceSq = searchRange * searchRange;
        return targetItemEntity.distanceToSqr(villager) <= maxDistanceSq;
    }

    @Override
    public void start() {
        if (targetItemEntity == null) return;
        UUID targetUuid = targetItemEntity.getUUID();
        villager.getPersistentData().putUUID(NBT_TARGET_ITEM_UUID, targetUuid);
    }

    @Override
    public void stop() {
        villager.getNavigation().stop();
        villager.getPersistentData().remove(NBT_TARGET_ITEM_UUID);
        targetItemEntity = null;
    }

    @Override
    public void tick() {
        if (targetItemEntity == null) return;

        villager.getLookControl().setLookAt(targetItemEntity, 30.0f, 30.0f);
        villager.getNavigation().moveTo(targetItemEntity, moveSpeed);
    }

    private ItemEntity findNearestDrugItem() {
        AABB searchBox = villager.getBoundingBox().inflate(searchRange, 2.0, searchRange);

        List<ItemEntity> itemEntities = villager.level().getEntitiesOfClass(ItemEntity.class, searchBox, itemEntity -> itemEntity.isAlive()
                                && !itemEntity.getItem().isEmpty()
                                && DrugItemUtil.isDrugItem(itemEntity.getItem()));

        if (itemEntities.isEmpty()) return null;

        return itemEntities.stream()
                .min(Comparator.comparingDouble(itemEntity -> itemEntity.distanceToSqr(villager)))
                .orElse(null);
    }
}