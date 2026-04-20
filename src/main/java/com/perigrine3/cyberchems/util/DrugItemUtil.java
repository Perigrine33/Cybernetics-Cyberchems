package com.perigrine3.cyberchems.util;

import com.perigrine3.cyberchems.potions.ModPotions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class DrugItemUtil {
    private DrugItemUtil() {}

    public static boolean isDrugItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModTags.Items.ADDICTIVES)) return true;
        return isDrugPotion(stack);
    }

    public static boolean isDrugPotion(ItemStack stack) {
        if (!(stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))) {
            return false;
        }

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        var potionHolder = contents.potion().orElse(null);
        if (potionHolder == null) return false;

        return potionHolder.value() == ModPotions.ROID
                || potionHolder.value() == ModPotions.STIM
                || potionHolder.value() == ModPotions.BLACKLACE;
    }
}