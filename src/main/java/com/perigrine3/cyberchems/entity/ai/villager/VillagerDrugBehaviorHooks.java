package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import com.perigrine3.cyberchems.util.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class VillagerDrugBehaviorHooks {

    private static final String NBT_GOALS_ADDED = "cc_addicted_goals_added";

    private static final String NBT_ROID_RAGE_ROLLED = "cc_roid_rage_rolled";
    private static final String NBT_ROID_RAGE_ACTIVE = "cc_roid_rage_active";

    private static final float ROID_RAGE_CHANCE = 0.75f;

    private VillagerDrugBehaviorHooks() {}

    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;

        CompoundTag persistentData = villager.getPersistentData();
        if (persistentData.getBoolean(NBT_GOALS_ADDED)) return;
        persistentData.putBoolean(NBT_GOALS_ADDED, true);

        villager.goalSelector.addGoal(2, new DrugRushPickupGoal(villager, 1.25D, 18.0D));
    }

    @SubscribeEvent
    public static void onVillagerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;
        if (villager.hasEffect(ModEffects.ADDICTION)) {
            if ((villager.tickCount % 10) == 0) {
                consumeOneDrugIfPresent(villager);
            }
        }

        handleRoidRage(villager);
    }

    private static void consumeOneDrugIfPresent(Villager villager) {
        var inventory = villager.getInventory();

        for (int slotIndex = 0; slotIndex < inventory.getContainerSize(); slotIndex++) {
            ItemStack stackInSlot = inventory.getItem(slotIndex);
            if (stackInSlot.isEmpty()) continue;
            if (!DrugItemUtil.isDrugItem(stackInSlot)) continue;

            boolean wasAutoinjector = stackInSlot.is(ModTags.Items.ADDICTIVES);
            ItemStack oneItem = stackInSlot.split(1);
            ItemStack remainder = oneItem.finishUsingItem(villager.level(), villager);

            if (!remainder.isEmpty()) {
                inventory.addItem(remainder);
            }

            if (wasAutoinjector) {
                ItemStack emptyInjector = new ItemStack(ModItems.EMPTY_AUTOINJECTOR.get());
                villager.spawnAtLocation(emptyInjector);
            }

            return;
        }
    }

    private static void handleRoidRage(Villager villager) {
        MobEffectInstance roidInstance = villager.getEffect(ModEffects.ROID);
        if (roidInstance == null) {
            CompoundTag persistentData = villager.getPersistentData();
            persistentData.remove(NBT_ROID_RAGE_ROLLED);
            persistentData.remove(NBT_ROID_RAGE_ACTIVE);
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

        if (!persistentData.getBoolean(NBT_ROID_RAGE_ACTIVE)) return;
        if ((villager.tickCount % 20) != 0) return;

        ensureVillagerCombatGoals(villager);
    }

    private static void ensureVillagerCombatGoals(Villager villager) {
        CompoundTag persistentData = villager.getPersistentData();
        if (persistentData.getBoolean("cc_has_combat_goals")) return;

        persistentData.putBoolean("cc_has_combat_goals", true);

        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.25D, true));
        villager.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(villager, LivingEntity.class, 10,
                true, false, candidate -> candidate != null && candidate.isAlive() && candidate != villager));
    }
}