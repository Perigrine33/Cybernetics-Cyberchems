package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.common.humanity.DataIntegrityHandler;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.compat.CyberneticsHumanityCompat;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Cyberchems.MODID)
public final class DrugHumanityPenaltyCleanupHooks {

    private static final String ROID_KEY =
            "cc_drug_penalty_roid";

    private static final String STIM_KEY =
            "cc_drug_penalty_stim";

    private static final String BLACKLACE_KEY =
            "cc_drug_penalty_black_lace";

    private static final String IMMUNOBOOST_KEY =
            "cc_drug_penalty_immunoboost";

    private static final String ADDICTION_KEY =
            "cc_drug_penalty_addiction";

    private DrugHumanityPenaltyCleanupHooks() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player =
                event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        if (DataIntegrityHandler.usesDataIntegrity(player)) {
            clearAllDrugPenalties(player);
            return;
        }

        if (!player.hasEffect(ModEffects.ROID)) {
            CyberneticsHumanityCompat.clearPenalty(
                    player,
                    ROID_KEY
            );
        }

        if (!player.hasEffect(ModEffects.STIM)) {
            CyberneticsHumanityCompat.clearPenalty(
                    player,
                    STIM_KEY
            );
        }

        if (!player.hasEffect(ModEffects.BLACKLACE)) {
            CyberneticsHumanityCompat.clearPenalty(
                    player,
                    BLACKLACE_KEY
            );
        }

        if (!player.hasEffect(ModEffects.IMMUNOBOOST)) {
            CyberneticsHumanityCompat.clearPenalty(
                    player,
                    IMMUNOBOOST_KEY
            );
        }

        if (!player.hasEffect(ModEffects.ADDICTION)) {
            CyberneticsHumanityCompat.clearPenalty(
                    player,
                    ADDICTION_KEY
            );
        }
    }

    private static void clearAllDrugPenalties(Player player) {
        CyberneticsHumanityCompat.clearPenalty(
                player,
                ROID_KEY
        );

        CyberneticsHumanityCompat.clearPenalty(
                player,
                STIM_KEY
        );

        CyberneticsHumanityCompat.clearPenalty(
                player,
                BLACKLACE_KEY
        );

        CyberneticsHumanityCompat.clearPenalty(
                player,
                IMMUNOBOOST_KEY
        );

        CyberneticsHumanityCompat.clearPenalty(
                player,
                ADDICTION_KEY
        );
    }
}