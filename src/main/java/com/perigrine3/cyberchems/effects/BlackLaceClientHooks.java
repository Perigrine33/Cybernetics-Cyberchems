package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Cyberchems.MODID, value = Dist.CLIENT)
public final class BlackLaceClientHooks {

    private BlackLaceClientHooks() {}

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (event.getName() != VanillaGuiLayers.PLAYER_HEALTH) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.getEffect(ModEffects.BLACKLACE) != null) {
            event.setCanceled(true);
        }
    }
}