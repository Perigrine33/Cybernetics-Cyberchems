package com.perigrine3.cyberchems.util;

import com.perigrine3.cyberchems.item.ModItems;
import com.perigrine3.cyberchems.potions.ModPotions;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potion;

public final class DrugItemUtil {
    private DrugItemUtil() {}

    public static boolean isDrugItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return isDrugAutoinjector(stack) || isDrugPotion(stack);
    }

    public static boolean isDrugAutoinjector(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return stack.is(ModItems.ROID_AUTOINJECTOR.get())
                || stack.is(ModItems.STIM_AUTOINJECTOR.get())
                || stack.is(ModItems.BLACKLACE_AUTOINJECTOR.get())
                || stack.is(ModItems.IMMUNOBOOST_AUTOINJECTOR.get())
                || stack.is(ModItems.WARP_AUTOINJECTOR.get())
                || stack.is(ModItems.ADDICTOL_AUTOINJECTOR.get());
    }

    public static boolean isDrugPotion(ItemStack stack) {
        Holder<Potion> potionHolder = getPotionHolder(stack);
        if (potionHolder == null) return false;

        return potionHolder.equals(ModPotions.ROID)
                || potionHolder.equals(ModPotions.STIM)
                || potionHolder.equals(ModPotions.BLACKLACE);
    }

    public static boolean isRoidItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.ROID_AUTOINJECTOR.get())) return true;

        Holder<Potion> potionHolder = getPotionHolder(stack);
        return potionHolder != null && potionHolder.equals(ModPotions.ROID);
    }

    public static boolean isStimItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.STIM_AUTOINJECTOR.get())) return true;

        Holder<Potion> potionHolder = getPotionHolder(stack);
        return potionHolder != null && potionHolder.equals(ModPotions.STIM);
    }

    public static boolean isBlacklaceItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.BLACKLACE_AUTOINJECTOR.get())) return true;

        Holder<Potion> potionHolder = getPotionHolder(stack);
        return potionHolder != null && potionHolder.equals(ModPotions.BLACKLACE);
    }

    public static boolean isRegularPotion(ItemStack stack) {
        return stack.is(Items.POTION);
    }

    public static boolean isSplashPotion(ItemStack stack) {
        return stack.is(Items.SPLASH_POTION);
    }

    public static boolean isLingeringPotion(ItemStack stack) {
        return stack.is(Items.LINGERING_POTION);
    }

    private static Holder<Potion> getPotionHolder(ItemStack stack) {
        if (!(stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))) {
            return null;
        }

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return null;

        return contents.potion().orElse(null);
    }
}