package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.AddictionEffect;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class VillagerDrugBehaviorHooks {

    private static final String NBT_GOALS_ADDED = "cc_addicted_goals_added";
    private static final String NBT_TARGET_ITEM_UUID = "cc_addicted_target_drug_item_uuid";

    private static final String NBT_ROID_RAGE_ROLLED = "cc_roid_rage_rolled";
    private static final String NBT_ROID_RAGE_ACTIVE = "cc_roid_rage_active";

    private static final String NBT_FLEE_UNTIL_TICK = "cc_addicted_flee_until_tick";
    private static final String NBT_HAS_COMBAT_GOALS = "cc_has_combat_goals";

    private static final float ROID_RAGE_CHANCE = 0.75f;

    private static final double DRUG_SEARCH_RANGE = 18.0D;
    private static final double DRUG_SEARCH_Y = 4.0D;
    private static final double DRUG_MOVE_SPEED = 0.75D;
    private static final double DRUG_PICKUP_DISTANCE_SQ = 1.0D;
    private static final double DRUG_PULL_STRENGTH = 0.08D;

    private static final int FLEE_DURATION_TICKS = 80;
    private static final double FLEE_SPEED = 0.75D;

    private static final ResourceLocation ROID_RAGE_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "villager_roid_rage_speed");
    private static final double ROID_RAGE_SPEED_BONUS = 0.35D;

    private VillagerDrugBehaviorHooks() {}

    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;

        CompoundTag persistentData = villager.getPersistentData();
        if (persistentData.getBoolean(NBT_GOALS_ADDED)) return;
        persistentData.putBoolean(NBT_GOALS_ADDED, true);

        villager.setCanPickUpLoot(true);
    }

    @SubscribeEvent
    public static void onVillagerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;

        if (isCurrentlyFleeing(villager)) {
            handleRoidRage(villager);
            return;
        }

        if (villager.hasEffect(ModEffects.ADDICTION)) {
            villager.setCanPickUpLoot(true);
            handleDrugRush(villager);
        } else {
            villager.getPersistentData().remove(NBT_TARGET_ITEM_UUID);
            villager.getNavigation().stop();
        }

        handleRoidRage(villager);
    }

    private static boolean isCurrentlyFleeing(Villager villager) {
        return villager.tickCount < villager.getPersistentData().getInt(NBT_FLEE_UNTIL_TICK);
    }

    private static void handleDrugRush(Villager villager) {
        ItemEntity nearestDrug = findNearestDrugItem(villager);
        if (nearestDrug == null) {
            villager.getPersistentData().remove(NBT_TARGET_ITEM_UUID);
            villager.getNavigation().stop();
            return;
        }

        villager.getPersistentData().putUUID(NBT_TARGET_ITEM_UUID, nearestDrug.getUUID());

        villager.setTarget(null);
        villager.setCanPickUpLoot(true);

        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.PATH);
        villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

        villager.getLookControl().setLookAt(nearestDrug, 30.0f, 30.0f);
        villager.getMoveControl().setWantedPosition(nearestDrug.getX(), nearestDrug.getY(), nearestDrug.getZ(), DRUG_MOVE_SPEED);
        villager.getNavigation().moveTo(nearestDrug.getX(), nearestDrug.getY(), nearestDrug.getZ(), DRUG_MOVE_SPEED);

        Vec3 toward = nearestDrug.position().subtract(villager.position());
        Vec3 horizontal = new Vec3(toward.x, 0.0D, toward.z);

        if (horizontal.lengthSqr() > 0.0001D) {
            Vec3 push = horizontal.normalize().scale(DRUG_PULL_STRENGTH);
            villager.setDeltaMovement(villager.getDeltaMovement().add(push));
            villager.hurtMarked = true;
        }

        if (villager.distanceToSqr(nearestDrug) <= DRUG_PICKUP_DISTANCE_SQ) {
            pickUpConsumeAndFlee(villager, nearestDrug);
            villager.getPersistentData().remove(NBT_TARGET_ITEM_UUID);
            villager.getNavigation().stop();
        }
    }

    private static ItemEntity findNearestDrugItem(Villager villager) {
        AABB searchBox = villager.getBoundingBox().inflate(DRUG_SEARCH_RANGE, DRUG_SEARCH_Y, DRUG_SEARCH_RANGE);

        List<ItemEntity> itemEntities = villager.level().getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                itemEntity -> itemEntity.isAlive()
                        && !itemEntity.getItem().isEmpty()
                        && DrugItemUtil.isDrugItem(itemEntity.getItem())
        );

        if (itemEntities.isEmpty()) {
            return null;
        }

        return itemEntities.stream()
                .min(Comparator.comparingDouble(itemEntity -> itemEntity.distanceToSqr(villager)))
                .orElse(null);
    }

    private static void pickUpConsumeAndFlee(Villager villager, ItemEntity itemEntity) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) return;
        if (!DrugItemUtil.isDrugItem(groundStack)) return;

        Vec3 pickupPos = itemEntity.position();

        ItemStack oneDrug = groundStack.split(1);

        if (groundStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(groundStack);
        }

        consumeImmediately(villager, oneDrug);
        flee(villager, pickupPos);
    }

    private static void consumeImmediately(Villager villager, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!DrugItemUtil.isDrugItem(stack)) return;

        applyDrugEffectDirectly(villager, stack);

        if (DrugItemUtil.isDrugAutoinjector(stack)) {
            villager.spawnAtLocation(new ItemStack(ModItems.EMPTY_AUTOINJECTOR.get()));
            return;
        }

        if (DrugItemUtil.isRegularPotion(stack)) {
            villager.spawnAtLocation(new ItemStack(Items.GLASS_BOTTLE));
        }
    }

    private static void applyDrugEffectDirectly(Villager villager, ItemStack stack) {
        Holder<MobEffect> effect = null;

        if (DrugItemUtil.isRoidItem(stack)) {
            effect = ModEffects.ROID;
        } else if (DrugItemUtil.isStimItem(stack)) {
            effect = ModEffects.STIM;
        } else if (DrugItemUtil.isBlacklaceItem(stack)) {
            effect = ModEffects.BLACKLACE;
        }

        if (effect == null) {
            return;
        }

        MobEffectInstance existing = villager.getEffect(effect);
        int duration = existing != null
                ? Math.max(existing.getDuration(), AddictionEffect.DURATION_7_DAYS_TICKS)
                : AddictionEffect.DURATION_7_DAYS_TICKS;
        int amplifier = existing != null ? existing.getAmplifier() : 0;

        villager.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
    }

    private static void flee(Villager villager, Vec3 fromPos) {
        villager.getPersistentData().putInt(NBT_FLEE_UNTIL_TICK, villager.tickCount + FLEE_DURATION_TICKS);

        Vec3 away = villager.position().subtract(fromPos);
        Vec3 horizontalAway = new Vec3(away.x, 0.0D, away.z);

        BlockPos targetPos;
        if (horizontalAway.lengthSqr() > 0.0001D) {
            Vec3 dir = horizontalAway.normalize().scale(10.0D);
            targetPos = BlockPos.containing(
                    villager.getX() + dir.x,
                    villager.getY(),
                    villager.getZ() + dir.z
            );
        } else {
            int dx = villager.getRandom().nextInt(17) - 8;
            int dz = villager.getRandom().nextInt(17) - 8;
            targetPos = villager.blockPosition().offset(dx, 0, dz);
        }

        villager.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), FLEE_SPEED);
    }

    private static void handleRoidRage(Villager villager) {
        MobEffectInstance roidInstance = villager.getEffect(ModEffects.ROID);
        if (roidInstance == null) {
            CompoundTag persistentData = villager.getPersistentData();
            persistentData.remove(NBT_ROID_RAGE_ROLLED);
            persistentData.remove(NBT_ROID_RAGE_ACTIVE);
            removeRoidRageSpeed(villager);
            return;
        }

        CompoundTag persistentData = villager.getPersistentData();

        if (!persistentData.getBoolean(NBT_ROID_RAGE_ROLLED)) {
            boolean rageActive = villager.getRandom().nextFloat() < ROID_RAGE_CHANCE;
            persistentData.putBoolean(NBT_ROID_RAGE_ACTIVE, rageActive);
            persistentData.putBoolean(NBT_ROID_RAGE_ROLLED, true);

            if (rageActive) {
                ensureVillagerCombatGoals(villager);
            }
        }

        if (!persistentData.getBoolean(NBT_ROID_RAGE_ACTIVE)) {
            removeRoidRageSpeed(villager);
            return;
        }

        applyRoidRageSpeed(villager);

        if ((villager.tickCount % 20) != 0) return;
        ensureVillagerCombatGoals(villager);
    }

    private static void applyRoidRageSpeed(Villager villager) {
        AttributeInstance movementSpeed = villager.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        if (movementSpeed.getModifier(ROID_RAGE_SPEED_ID) == null) {
            movementSpeed.addPermanentModifier(new AttributeModifier(
                    ROID_RAGE_SPEED_ID,
                    ROID_RAGE_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private static void removeRoidRageSpeed(Villager villager) {
        AttributeInstance movementSpeed = villager.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        movementSpeed.removeModifier(ROID_RAGE_SPEED_ID);
    }

    private static void ensureVillagerCombatGoals(Villager villager) {
        CompoundTag persistentData = villager.getPersistentData();
        if (persistentData.getBoolean(NBT_HAS_COMBAT_GOALS)) return;

        persistentData.putBoolean(NBT_HAS_COMBAT_GOALS, true);

        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.25D, true));
        villager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                villager,
                LivingEntity.class,
                10,
                true,
                false,
                candidate -> candidate != null && candidate.isAlive() && candidate != villager
        ));
    }
}