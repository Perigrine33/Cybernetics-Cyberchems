package com.perigrine3.cyberchems.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.cyberchems.Cyberchems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class RoidEffect extends MobEffect {

    private static final String HUMANITY_KEY = "cc_drug_penalty_roid";

    private static final int HUMANITY_PENALTY_BASE = 10;
    private static final int HUMANITY_PENALTY_PER_EXTRA = 5;

    private static final int CRASH_TICKS = 5 * 60 * 20;

    private static final String ROID_REDOSE_GUARD = "cc_roid_redose_guard_until";

    // Modifier ids (ResourceLocation) — stable and used for lookup/removal
    private static final ResourceLocation ROID_ATTACK =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_damage");
    private static final ResourceLocation ROID_SPEED =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_speed");
    private static final ResourceLocation ROID_SIZE =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_size");
    private static final ResourceLocation ROID_ATTACKSPEED =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_attackspeed");
    private static final ResourceLocation ROID_KNOCKBACK =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_knockback");
    private static final ResourceLocation ROID_RESIST =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_resist");
    private static final ResourceLocation ROID_EXPLOSIONRESIST =
            ResourceLocation.fromNamespaceAndPath(Cyberchems.MODID, "roid_explosionresist");

    // Base amounts (amp0 => these values). Scales by (amplifier + 1).
    private static final double BASE_ATTACK_DAMAGE = 7.0D;
    private static final double BASE_MOVE_SPEED = 0.01D;
    private static final double BASE_ATTACK_SPEED = 0.5D;
    private static final double BASE_ATTACK_KNOCKBACK = 0.5D;
    private static final double BASE_KNOCKBACK_RESIST = 3.0D;
    private static final double BASE_EXPLOSION_KNOCKBACK_RESIST = 3.0D;

    public RoidEffect(MobEffectCategory category, int color) {
        super(category, color);

        // Non-scaling as requested
        this.addAttributeModifier(
                Attributes.SCALE,
                ROID_SIZE,
                0.1D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof Player player)) return true;
        if (player.level().isClientSide) return true;

        applyHumanityPenalty(player, amplifier);

        // Apply/update scaling modifiers each tick (server)
        applyOrUpdateScaledModifiers(player, amplifier);

        // Your existing cleanse + redose guard
        if ((player.tickCount % 20) == 0) {
            if (player.hasEffect(MobEffects.WEAKNESS) || player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                MobEffectInstance weak = player.getEffect(MobEffects.WEAKNESS);
                MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

                boolean removedAny = false;
                if (weak != null && weak.getDuration() > (CRASH_TICKS / 2)) {
                    player.removeEffect(MobEffects.WEAKNESS);
                    removedAny = true;
                }
                if (slow != null && slow.getDuration() > (CRASH_TICKS / 2)) {
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    removedAny = true;
                }

                if (removedAny) {
                    long until = player.level().getGameTime() + 40;
                    player.getPersistentData().putLong(ROID_REDOSE_GUARD, until);
                }
            }
        }

        return true;
    }

    @Override
    public void onMobRemoved(LivingEntity living, int amplifier, Entity.RemovalReason reason) {
        if (!(living instanceof Player player)) return;
        if (player.level().isClientSide) return;

        clearHumanityPenalty(player);

        // Remove our scaled modifiers when effect ends
        removeScaledModifiers(player);

        long guardUntil = player.getPersistentData().getLong(ROID_REDOSE_GUARD);
        long now = player.level().getGameTime();
        if (guardUntil >= now) return;

        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, CRASH_TICKS, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CRASH_TICKS, 0, false, false, false));
    }

    private static void applyHumanityPenalty(Player player, int amplifier) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        int penalty = HUMANITY_PENALTY_BASE + (amplifier * HUMANITY_PENALTY_PER_EXTRA);
        data.setHumanityPenalty(HUMANITY_KEY, penalty);
    }

    private static void clearHumanityPenalty(Player player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        data.clearHumanityPenalty(HUMANITY_KEY);
    }

    private static void applyOrUpdateScaledModifiers(Player player, int amplifier) {
        int level = amplifier + 1;

        upsert(player.getAttribute(Attributes.ATTACK_DAMAGE), ROID_ATTACK,
                BASE_ATTACK_DAMAGE * level, AttributeModifier.Operation.ADD_VALUE);

        upsert(player.getAttribute(Attributes.MOVEMENT_SPEED), ROID_SPEED,
                BASE_MOVE_SPEED * level, AttributeModifier.Operation.ADD_VALUE);

        upsert(player.getAttribute(Attributes.ATTACK_SPEED), ROID_ATTACKSPEED,
                BASE_ATTACK_SPEED * level, AttributeModifier.Operation.ADD_VALUE);

        upsert(player.getAttribute(Attributes.ATTACK_KNOCKBACK), ROID_KNOCKBACK,
                BASE_ATTACK_KNOCKBACK * level, AttributeModifier.Operation.ADD_VALUE);

        upsert(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), ROID_RESIST,
                BASE_KNOCKBACK_RESIST * level, AttributeModifier.Operation.ADD_VALUE);

        upsert(player.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE), ROID_EXPLOSIONRESIST,
                BASE_EXPLOSION_KNOCKBACK_RESIST * level, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void removeScaledModifiers(Player player) {
        remove(player.getAttribute(Attributes.ATTACK_DAMAGE), ROID_ATTACK);
        remove(player.getAttribute(Attributes.MOVEMENT_SPEED), ROID_SPEED);
        remove(player.getAttribute(Attributes.ATTACK_SPEED), ROID_ATTACKSPEED);
        remove(player.getAttribute(Attributes.ATTACK_KNOCKBACK), ROID_KNOCKBACK);
        remove(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), ROID_RESIST);
        remove(player.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE), ROID_EXPLOSIONRESIST);
    }

    private static void upsert(AttributeInstance inst, ResourceLocation id, double amount, AttributeModifier.Operation op) {
        if (inst == null) return;

        // AttributeInstance in 1.21.x stores modifiers keyed by ResourceLocation; use is(id) to match.
        AttributeModifier existing = find(inst, id);
        if (existing != null && existing.amount() == amount && existing.operation() == op) return;

        if (existing != null) {
            inst.removeModifier(id); // remove by ResourceLocation id
        }

        inst.addTransientModifier(new AttributeModifier(id, amount, op));
    }

    private static void remove(AttributeInstance inst, ResourceLocation id) {
        if (inst == null) return;
        if (find(inst, id) != null) {
            inst.removeModifier(id);
        }
    }

    private static AttributeModifier find(AttributeInstance inst, ResourceLocation id) {
        // Safe linear scan; small counts.
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.is(id)) return m;
        }
        return null;
    }
}