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
        // 触发Forge事件，以兼容其他模组
        if (attacker instanceof Player player && !ForgeHooks.onPlayerAttackTarget(player, target)) {
            return;
        }
        // 判断攻击目标是否可以被攻�?
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
     * 该方法伤害公�?(面板攻击�?+ 横扫之刃附魔加成 + 评分等级加成 + 杀手类附魔加成) * 连招伤害系数 * 拔刀伤害系数 * 拔刀剑伤害调整比�?* 暴击倍率
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
     * 横扫之刃附魔加成(三级加成3.25攻击�?
     */
    public static float getSweepingBonus(LivingEntity attacker) {
        return 10 * (EnchantmentHelper.getSweepingDamageRatio(attacker) * 0.5f);
    }

    /**
     * 评分等级加成
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
     * 杀手类附魔加成(杀死类附魔攻击对应的生物加�?.5 * 附魔等级)
     */
    public static float getEnchantmentBonus(LivingEntity attacker, Entity target) {
        if (target instanceof LivingEntity living) {
            return EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), living.getMobType());
        } else {
            return EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), MobType.UNDEFINED);
        }
    }

    /**
     * 计算击退
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
     * 判断是否暴击
     */
    public static boolean isCriticalHit(LivingEntity attacker, Entity target) {
        return attacker.fallDistance > 0.0F && !attacker.isOnGround() &&
                !attacker.onClimbable() && !attacker.isInWater() &&
                !attacker.hasEffect(MobEffects.BLINDNESS) &&
                !attacker.isPassenger() && target instanceof LivingEntity && !attacker.isSprinting();
    }

    /**
     * 火焰附加处理
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
     * 应用击退
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
     * 恢复目标原有速度（用于ServerPlayer�?
     */
    public static void restoreTargetMotionIfNeeded(Entity target, Vec3 originalMotion) {
        if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
            target.setDeltaMovement(originalMotion);
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            target.hurtMarked = false;
        }
    }

    /**
     * 播放攻击音效与暴击效�?
     */
    public static void playAttackEffects(LivingEntity attacker, Entity target, boolean isCritical) {
        attacker.getLevel().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
        if (isCritical && attacker instanceof Player player) {
            player.crit(target);
        }
    }

    /**
     * 处理附魔后置效果与耐久
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
        // 减少耐久
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
     * 处理攻击后效果（统计、火焰、粒子、饱食度�?
     */
    public static void handlePostAttackEffects(LivingEntity attacker, Entity target, FireAspectResult fireAspectResult) {
        if (target instanceof LivingEntity) {
            float damageDealt = fireAspectResult.preAttackHealth - ((LivingEntity) target).getHealth();
            //伤害统计
            if (attacker instanceof Player player) {
                player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
            }
            //应用完整的火焰附加效�?每级4�?
            if (fireAspectResult.fireAspectLevel > 0) {
                target.setSecondsOnFire(fireAspectResult.fireAspectLevel * 4);
            }
            // 伤害粒子
            if (attacker.getLevel() instanceof ServerLevel && damageDealt > 2.0F) {
                int k = (int) (damageDealt * 0.5D);
                ((ServerLevel) attacker.getLevel()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
            }
        }
        // 消耗饱食度
        if (attacker instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
        }
    }

    /**
     * 处理攻击未成功的情况
     */
    public static void handleFailedAttack(LivingEntity attacker, Entity target, FireAspectResult fireAspectResult) {
        attacker.getLevel().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
        if (fireAspectResult.shouldSetFire) {
            //取消预火焰附加效�?
            target.clearFire();
        }
    }
}






