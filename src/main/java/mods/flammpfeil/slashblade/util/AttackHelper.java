package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import static mods.flammpfeil.slashblade.SlashBladeConfig.REFINE_DAMAGE_MULTIPLIER;
import static mods.flammpfeil.slashblade.SlashBladeConfig.SLASHBLADE_DAMAGE_MULTIPLIER;
import static mods.flammpfeil.slashblade.util.AttackManager.getSlashBladeDamageScale;

public class AttackHelper {
    public static void attack(LivingEntity attacker, Entity target, float comboRatio) {
        // Forge
        if (attacker instanceof Player player && !ForgeHooks.onPlayerAttackTarget(player, target)) {
            return;
        }
        // ?
        if (!target.isAttackable() || target.skipAttackInteraction(attacker)) {
            return;
        }

        boolean isCritical = isCriticalHit(attacker, target);
        double baseDamage = calculateTotalDamage(attacker, target, comboRatio, isCritical);

        if (baseDamage <= 0.0F) {
            return;
        }

        float knockback = calculateKnockback(attacker);

        FireAspectResult fireAspectResult = handleFireAspect(attacker, target);

        Vec3 originalMotion = target.getDeltaMovement();

        DamageSource damageSource;
        if (attacker instanceof Player player) {
            damageSource = DamageSource.playerAttack(player);
        } else {
            damageSource = DamageSource.mobAttack(attacker);
        }

        boolean damageSuccess = target.hurt(damageSource, (float) baseDamage);

        if (damageSuccess) {
            applyKnockback(attacker, target, knockback);
            restoreTargetMotionIfNeeded(target, originalMotion);
            playAttackEffects(attacker, target, isCritical);
            handleEnchantmentsAndDurability(attacker, target);
            handlePostAttackEffects(attacker, target, fireAspectResult);
        } else {
            handleFailedAttack(attacker, target, fireAspectResult);
        }
    }

    /**
     * ?(?+  +  + ) *  *  * ?* 
     */
    public static double calculateTotalDamage(LivingEntity attacker, Entity target, float comboRatio, boolean isCritical) {
        double baseDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

        baseDamage += getSweepingBonus(attacker);
        baseDamage += getRankBonus(attacker);
        baseDamage += getEnchantmentBonus(attacker, target);
        baseDamage *= comboRatio * getSlashBladeDamageScale(attacker) * SLASHBLADE_DAMAGE_MULTIPLIER.get();

        if (attacker instanceof Player player) {
            CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, isCritical, isCritical ? 1.5F : 1.0F);
            isCritical = hitResult != null;
            if (isCritical) {
                baseDamage *= hitResult.getDamageModifier();
            }
        }
        return baseDamage;
    }

    /**
     * (3.25?
     */
    public static float getSweepingBonus(LivingEntity attacker) {
        return 10 * (EnchantmentHelper.getSweepingDamageRatio(attacker) * 0.5f);
    }

    /**
     * 
     */
    public static float getRankBonus(LivingEntity attacker) {
        IConcentrationRank.ConcentrationRanks rankBonus = attacker
                .getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                .map(rp -> rp.getRank(attacker.getCommandSenderWorld().getGameTime()))
                .orElse(IConcentrationRank.ConcentrationRanks.NONE);
        double rankDamageBonus = rankBonus.level / 2.0;
        if (IConcentrationRank.ConcentrationRanks.S.level <= rankBonus.level) {
            int refine = attacker.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).map(ISlashBladeState::getRefine).orElse(0);
            int level = 0;
            if (attacker instanceof Player player) {
                level = player.experienceLevel;
            }
            rankDamageBonus = Math.max(rankDamageBonus, Math.min(level, refine) * REFINE_DAMAGE_MULTIPLIER.get());
        }
        return (float) rankDamageBonus;
    }

    /**
     * (?.5 * )
     */
    public static float getEnchantmentBonus(LivingEntity attacker, Entity target) {
        if (target instanceof LivingEntity living) {
            return EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), living.getMobType());
        } else {
            return EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), MobType.UNDEFINED);
        }
    }

    /**
     * 
     */
    public static float calculateKnockback(LivingEntity attacker) {
        float knockback = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        knockback += EnchantmentHelper.getKnockbackBonus(attacker);
        if (attacker.isSprinting()) {
            attacker.getLevel().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, attacker.getSoundSource(), 1.0F, 1.0F);
            ++knockback;
        }
        return knockback;
    }

    /**
     * 
     */
    public static boolean isCriticalHit(LivingEntity attacker, Entity target) {
        return attacker.fallDistance > 0.0F && !attacker.isOnGround() &&
                !attacker.onClimbable() && !attacker.isInWater() &&
                !attacker.hasEffect(MobEffects.BLINDNESS) &&
                !attacker.isPassenger() && target instanceof LivingEntity && !attacker.isSprinting();
    }

    /**
     * 
     */
    public static class FireAspectResult {
        final float preAttackHealth;
        final boolean shouldSetFire;
        final int fireAspectLevel;

        FireAspectResult(float preAttackHealth, boolean shouldSetFire, int fireAspectLevel) {
            this.preAttackHealth = preAttackHealth;
            this.shouldSetFire = shouldSetFire;
            this.fireAspectLevel = fireAspectLevel;
        }
    }

    public static FireAspectResult handleFireAspect(LivingEntity attacker, Entity target) {
        float preAttackHealth = 0.0F;
        boolean shouldSetFire = false;
        int fireAspectLevel = EnchantmentHelper.getFireAspect(attacker);
        if (target instanceof LivingEntity living) {
            preAttackHealth = living.getHealth();
            if (fireAspectLevel > 0 && !target.isOnFire()) {
                shouldSetFire = true;
                target.setSecondsOnFire(1);
            }
        }
        return new FireAspectResult(preAttackHealth, shouldSetFire, fireAspectLevel);
    }

    /**
     * 
     */
    public static void applyKnockback(LivingEntity attacker, Entity target, float knockback) {
        if (knockback > 0) {
            if (target instanceof LivingEntity living) {
                living.knockback(knockback * 0.5D, Mth.sin(attacker.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(attacker.getYRot() * ((float) Math.PI / 180F)));
            } else {
                target.push(-Mth.sin(attacker.getYRot() * ((float) Math.PI / 180F)) * knockback * 0.5D, 0.1D, Mth.cos(attacker.getYRot() * ((float) Math.PI / 180F)) * knockback * 0.5D);
            }
            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            attacker.setSprinting(false);
        }
    }

    /**
     * erverPlayer?
     */
    public static void restoreTargetMotionIfNeeded(Entity target, Vec3 originalMotion) {
        if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
            target.setDeltaMovement(originalMotion);
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            target.hurtMarked = false;
        }
    }

    /**
     * ?
     */
    public static void playAttackEffects(LivingEntity attacker, Entity target, boolean isCritical) {
        attacker.getLevel().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
        if (isCritical && attacker instanceof Player player) {
            player.crit(target);
        }
    }

    /**
     * 
     */
    public static void handleEnchantmentsAndDurability(LivingEntity attacker, Entity target) {
        attacker.setLastHurtMob(target);
        if (target instanceof LivingEntity living) {
            EnchantmentHelper.doPostHurtEffects(living, attacker);
        }
        EnchantmentHelper.doPostDamageEffects(attacker, target);

        ItemStack itemStack = attacker.getMainHandItem();
        Entity entity = target;
        if (target instanceof PartEntity<?> partEntity) {
            entity = partEntity.getParent();
        }
        // 
        if (!attacker.getLevel().isClientSide() && !itemStack.isEmpty() && entity instanceof LivingEntity living) {
            ItemStack copy = itemStack.copy();
            Item item = itemStack.getItem();
            if (item.hurtEnemy(itemStack, living, attacker) && attacker instanceof Player player) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }
            if (itemStack.isEmpty()) {
                if (attacker instanceof Player player) {
                    ForgeEventFactory.onPlayerDestroyItem(player, copy, InteractionHand.MAIN_HAND);
                }
                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    /**
     * ?
     */
    public static void handlePostAttackEffects(LivingEntity attacker, Entity target, FireAspectResult fireAspectResult) {
        if (target instanceof LivingEntity) {
            float damageDealt = fireAspectResult.preAttackHealth - ((LivingEntity) target).getHealth();
            //
            if (attacker instanceof Player player) {
                player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
            }
            //?4?
            if (fireAspectResult.fireAspectLevel > 0) {
                target.setSecondsOnFire(fireAspectResult.fireAspectLevel * 4);
            }
            // 
            if (attacker.getLevel() instanceof ServerLevel && damageDealt > 2.0F) {
                int k = (int) (damageDealt * 0.5D);
                ((ServerLevel) attacker.getLevel()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
            }
        }
        // 
        if (attacker instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
        }
    }

    /**
     * 
     */
    public static void handleFailedAttack(LivingEntity attacker, Entity target, FireAspectResult fireAspectResult) {
        attacker.getLevel().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
        if (fireAspectResult.shouldSetFire) {
            //?
            target.clearFire();
        }
    }
}






