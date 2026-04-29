package com.perigrine3.cyberchems.item.cyberdrugs;

import com.perigrine3.createcybernetics.api.ISpinalInjectableItem;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.cyberchems.effects.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlackLaceAutoinjector extends Item implements ISpinalInjectableItem {

    private static final int CHARGE_TICKS = 16;
    private static final int EFFECT_DURATION = 24000;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final int MAX_EFFECT_AMPLIFIER = 3;

    public BlackLaceAutoinjector(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.cyberchems.blacklace_autoinjector.duration").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable("item.cyberchems.blacklace_autoinjector.desc").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int timeLeft) {
        if (!(living instanceof Player player)) return;

        int used = getUseDuration(stack, living) - timeLeft;
        if (used != CHARGE_TICKS) return;

        player.stopUsingItem();

        if (level.isClientSide) return;

        applyDose(player);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.25F, 1.2F);

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);

            ItemStack empty = new ItemStack(ModItems.EMPTY_AUTOINJECTOR.get());
            if (stack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), empty);
            } else if (!player.getInventory().add(empty)) {
                player.drop(empty, false);
            }
        }
    }

    private static int getNextAmplifier(Player player) {
        MobEffectInstance current = player.getEffect(ModEffects.BLACKLACE);
        if (current == null) {
            return EFFECT_AMPLIFIER;
        }

        return Math.min(MAX_EFFECT_AMPLIFIER, current.getAmplifier() + 1);
    }

    private static void applyDose(Player player) {
        player.addEffect(new MobEffectInstance(
                ModEffects.BLACKLACE,
                EFFECT_DURATION,
                getNextAmplifier(player)
        ));
    }

    @Override
    public boolean shouldSpinalInjectorInject(ServerPlayer player, ItemStack stack) {
        MobEffectInstance current = player.getEffect(ModEffects.BLACKLACE);
        return current == null || current.getAmplifier() < MAX_EFFECT_AMPLIFIER;
    }

    @Override
    public void applySpinalInjection(ServerPlayer player, ItemStack stack) {
        applyDose(player);
    }

    @Override
    public List<MobEffectInstance> getSpinalInjectionEffects(ItemStack stack) {
        return List.of(new MobEffectInstance(ModEffects.BLACKLACE, EFFECT_DURATION, EFFECT_AMPLIFIER));
    }
}