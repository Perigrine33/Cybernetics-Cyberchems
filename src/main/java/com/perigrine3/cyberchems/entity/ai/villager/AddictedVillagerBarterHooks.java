package com.perigrine3.cyberchems.entity.ai.villager;

import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.effects.ModEffects;
import com.perigrine3.cyberchems.util.DrugItemUtil;
import com.perigrine3.cyberchems.util.ModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class AddictedVillagerBarterHooks {

    private static final int PAYMENT_AUTOINJECTOR_EMERALDS = 5;
    private static final int PAYMENT_BOTTLE_EMERALDS = 3;

    private AddictedVillagerBarterHooks() {}

    @SubscribeEvent
    public static void onVillagerInteracted(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (villager.level().isClientSide) return;
        if (!villager.hasEffect(ModEffects.ADDICTION)) return;

        InteractionHand interactionHand = event.getHand();
        ItemStack heldStack = serverPlayer.getItemInHand(interactionHand);
        if (heldStack.isEmpty()) return;

        int emeraldPayment = getEmeraldPaymentForDrugItem(heldStack);
        if (emeraldPayment <= 0) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        ItemStack tradedDrugItem = heldStack.split(1);
        ItemStack emeraldStack = new ItemStack(Items.EMERALD, emeraldPayment);
        if (!serverPlayer.getInventory().add(emeraldStack)) {
            serverPlayer.drop(emeraldStack, false);
        }

        var villagerInventory = villager.getInventory();
        if (!villagerInventory.addItem(tradedDrugItem).isEmpty()) {
            villager.spawnAtLocation(tradedDrugItem);
        }
    }

    private static int getEmeraldPaymentForDrugItem(ItemStack stack) {
        if (stack.is(ModTags.Items.ADDICTIVES)) return PAYMENT_AUTOINJECTOR_EMERALDS;
        if (DrugItemUtil.isDrugPotion(stack)) return PAYMENT_BOTTLE_EMERALDS;

        return 0;
    }
}