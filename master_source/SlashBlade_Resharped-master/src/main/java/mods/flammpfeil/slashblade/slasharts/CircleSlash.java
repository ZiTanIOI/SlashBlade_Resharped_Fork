package mods.flammpfeil.slashblade.slasharts;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class CircleSlash {
    public static void doCircleSlashAttack(LivingEntity living, float yRot) {
        if (living.level().isClientSide()) {
            return;
        }

        ItemStack blade = living.getMainHandItem();
        if (!blade.getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return;
        }
        SlashBladeEvent.DoSlashEvent event = new SlashBladeEvent.DoSlashEvent(blade,
                blade.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new),
                living, 0, true, 0.325D, KnockBacks.cancel);
        event.setYRot(yRot);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }

        Vec3 pos = living.position().add(0.0D, (double) living.getEyeHeight() * 0.75D, 0.0D)
                .add(living.getLookAngle().scale(0.3f));

        pos = pos.add(VectorHelper.getVectorForRotation(-90.0F, living.getViewYRot(0)).scale(Vec3.ZERO.y))
                .add(VectorHelper.getVectorForRotation(0, living.getViewYRot(0) + 90).scale(Vec3.ZERO.z))
                .add(living.getLookAngle().scale(Vec3.ZERO.z));

        EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, living.level()) {

            @Override
            public SoundEvent getSlashSound() {
                return SoundEvents.EMPTY;
            }
        };
        jc.setPos(pos.x, pos.y, pos.z);
        jc.setOwner(event.getUser());

        jc.setRotationRoll(0);
        jc.setYRot(living.getYRot() - 22.5F + yRot);
        jc.setXRot(0);

        int colorCode = living.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                .map(ISlashBladeState::getColorCode).orElse(0xFFFFFF);
        jc.setColor(colorCode);

        jc.setMute(false);
        jc.setIsCritical(event.isCritical());

        jc.setDamage(event.getDamage());

        jc.setKnockBack(event.getKnockback());

        living.getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                .ifPresent(rank -> jc.setRank(rank.getRankLevel(living.level().getGameTime())));

        living.level().addFreshEntity(jc);
    }

}
