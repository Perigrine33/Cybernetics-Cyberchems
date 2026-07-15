package com.perigrine3.cyberchems.compat;

import com.perigrine3.createcybernetics.common.humanity.DataIntegrityHandler;
import com.perigrine3.createcybernetics.common.humanity.HumanityAttributeModifiers;
import net.minecraft.world.entity.player.Player;

public final class CyberneticsHumanityCompat {

    private CyberneticsHumanityCompat() {}

    public static boolean usesHumanity(Player player) {
        return player != null && !DataIntegrityHandler.usesDataIntegrity(player);
    }

    public static void setPenalty(Player player, String key, int penalty) {
        if (player == null || key == null || key.isBlank()) {
            return;
        }

        if (!usesHumanity(player) || penalty <= 0) {
            clearPenalty(player, key);
            return;
        }

        HumanityAttributeModifiers.setPenalty(player, key, penalty);
    }

    public static void clearPenalty(Player player, String key) {
        if (player == null || key == null || key.isBlank()) {
            return;
        }

        HumanityAttributeModifiers.clearPenalty(player, key);
    }
}