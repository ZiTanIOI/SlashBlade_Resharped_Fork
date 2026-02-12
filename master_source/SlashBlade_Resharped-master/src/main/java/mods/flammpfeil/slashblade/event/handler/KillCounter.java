package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KillCounter {
    private static final class SingletonHolder {
        private static final KillCounter instance = new KillCounter();
    }

    public static KillCounter getInstance() {
        return SingletonHolder.instance;
    }

    private KillCounter() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeathEvent(LivingDeathEvent event) {
        Entity trueSource = event.getSource().getEntity();

        if (!(trueSource instanceof LivingEntity)) {
            return;
        }

        ItemStack stack = ((LivingEntity) trueSource).getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
            return;
        }

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            var killCountEvent = new SlashBladeEvent.AddKillCountEvent(stack, state, 1);
            MinecraftForge.EVENT_BUS.post(killCountEvent);
            state.setKillCount(state.getKillCount() + killCountEvent.getNewCount());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onXPDropping(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
            return;
        }

        IConcentrationRank.ConcentrationRanks rankBonus = player
                .getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                .map(rp -> rp.getRank(player.getCommandSenderWorld().getGameTime()))
                .orElse(IConcentrationRank.ConcentrationRanks.NONE);
        int souls = (int) Math.floor(event.getDroppedExperience() * (1.0F + (rankBonus.level * 0.1F)));

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            var soulEvent = new SlashBladeEvent.AddProudSoulEvent(stack, state, Math.min(SlashBladeConfig.MAX_PROUD_SOUL_GOT.get(), souls));
            MinecraftForge.EVENT_BUS.post(soulEvent);
            int newCount = soulEvent.getNewCount();
            state.setProudSoulCount(
                    state.getProudSoulCount() + newCount);
            if (SwordType.from(stack).contains(SwordType.SOULEATER)) {
                int damage = Math.max(1, newCount / 4);
                stack.setDamageValue(Math.max(stack.getDamageValue() - damage, 0));
            }
        });

    }
}
