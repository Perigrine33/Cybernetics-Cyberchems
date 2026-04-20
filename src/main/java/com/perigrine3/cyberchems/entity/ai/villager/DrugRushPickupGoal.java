package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import com.perigrine3.cyberchems.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DrugRushPickupGoal extends Goal {

    public static final String NBT_TARGET_ITEM_UUID = "cc_addicted_target_drug_item_uuid";

    private static final double PICKUP_DISTANCE_SQ = 2.0D;
    private static final double FLEE_DISTANCE = 10.0D;

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
        if (targetItemEntity.getItem().isEmpty()) return false;

        return targetItemEntity.distanceToSqr(villager) <= (searchRange * searchRange);
    }

    @Override
    public void start() {
        if (targetItemEntity == null) return;

        UUID targetUuid = targetItemEntity.getUUID();
        villager.getPersistentData().putUUID(NBT_TARGET_ITEM_UUID, targetUuid);

        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    public void stop() {
        villager.getNavigation().stop();
        villager.getPersistentData().remove(NBT_TARGET_ITEM_UUID);
        targetItemEntity = null;
    }

    @Override
    public void tick() {
        if (targetItemEntity == null || !targetItemEntity.isAlive()) return;

        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);

        villager.getLookControl().setLookAt(targetItemEntity, 30.0f, 30.0f);
        villager.getNavigation().moveTo(targetItemEntity, moveSpeed);

        if (villager.distanceToSqr(targetItemEntity) <= PICKUP_DISTANCE_SQ) {
            pickUpConsumeAndFlee();
        }
    }

    private void pickUpConsumeAndFlee() {
        if (targetItemEntity == null || !targetItemEntity.isAlive()) {
            stop();
            return;
        }

        ItemStack groundStack = targetItemEntity.getItem();
        if (groundStack.isEmpty() || !DrugItemUtil.isDrugItem(groundStack)) {
            stop();
            return;
        }

        Vec3 itemPos = targetItemEntity.position();

        ItemStack oneDrug = groundStack.split(1);

        villager.take(targetItemEntity, 1);

        if (groundStack.isEmpty()) {
            targetItemEntity.discard();
        } else {
            targetItemEntity.setItem(groundStack);
        }

        consumeImmediately(oneDrug);
        fleeFrom(itemPos);

        stop();
    }

    private void consumeImmediately(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!DrugItemUtil.isDrugItem(stack)) return;

        boolean wasAutoinjector = stack.is(ModTags.Items.ADDICTIVES);

        ItemStack remainder = stack.finishUsingItem(villager.level(), villager);

        if (!remainder.isEmpty()) {
            ItemStack leftover = villager.getInventory().addItem(remainder);
            if (!leftover.isEmpty()) {
                villager.spawnAtLocation(leftover);
            }
        }

        if (wasAutoinjector) {
            villager.spawnAtLocation(new ItemStack(com.perigrine3.createcybernetics.item.ModItems.EMPTY_AUTOINJECTOR.get()));
        }
    }

    private void fleeFrom(Vec3 fromPos) {
        Vec3 away = villager.position().subtract(fromPos);
        Vec3 horizontalAway = new Vec3(away.x, 0.0D, away.z);

        BlockPos fleeTarget;

        if (horizontalAway.lengthSqr() > 0.0001D) {
            Vec3 dir = horizontalAway.normalize().scale(FLEE_DISTANCE);
            fleeTarget = BlockPos.containing(
                    villager.getX() + dir.x,
                    villager.getY(),
                    villager.getZ() + dir.z
            );
        } else {
            int dx = villager.getRandom().nextInt(17) - 8;
            int dz = villager.getRandom().nextInt(17) - 8;
            fleeTarget = villager.blockPosition().offset(dx, 0, dz);
        }

        villager.getNavigation().moveTo(fleeTarget.getX(), fleeTarget.getY(), fleeTarget.getZ(), moveSpeed * 1.15D);
    }

    private ItemEntity findNearestDrugItem() {
        AABB searchBox = villager.getBoundingBox().inflate(searchRange, 2.0D, searchRange);

        List<ItemEntity> itemEntities = villager.level().getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                itemEntity -> itemEntity.isAlive()
                        && !itemEntity.getItem().isEmpty()
                        && DrugItemUtil.isDrugItem(itemEntity.getItem())
        );

        if (itemEntities.isEmpty()) return null;

        return itemEntities.stream()
                .min(Comparator.comparingDouble(itemEntity -> itemEntity.distanceToSqr(villager)))
                .orElse(null);
    }
}