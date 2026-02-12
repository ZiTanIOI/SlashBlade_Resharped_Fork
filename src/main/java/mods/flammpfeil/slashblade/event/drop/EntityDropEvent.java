package mods.flammpfeil.slashblade.event.drop;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber
public class EntityDropEvent {
    @SubscribeEvent
    public static void dropBlade(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        try {
            var bladeRegistry = SlashBlade.getSlashBladeDefinitionRegistry(entity.getLevel());
            var dropEntryRegistry = entity.getLevel().registryAccess().registryOrThrow(EntityDropEntry.REGISTRY_KEY);
            dropEntryRegistry.forEach(entry -> {
                if (!ForgeRegistries.ENTITY_TYPES.containsKey(entry.entityType())) {
                    return;
                }
                if (!bladeRegistry.containsKey(entry.bladeName())) {
                    return;
                }

                if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
                    return;
                }

                if (SlashBladeConfig.FRIENDLY_ENABLE.get() || (entity instanceof Enemy)) {
                    if (entry.requestSlashBladeKill()
                            && !(attacker.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
                        return;
                    }
                }

                float resultRate = Math.min(1F, entry.dropRate() + event.getLootingLevel() * 0.1F);

                if (entry.dropFixedPoint()) {
                    dropBlade(entity, ForgeRegistries.ENTITY_TYPES.getValue(entry.entityType()),
                            Objects.requireNonNull(bladeRegistry.get(entry.bladeName())).getBlade(), resultRate, entry.dropPoint().x,
                            entry.dropPoint().y, entry.dropPoint().z);
                } else {
                    dropBlade(entity, ForgeRegistries.ENTITY_TYPES.getValue(entry.entityType()),
                            Objects.requireNonNull(bladeRegistry.get(entry.bladeName())).getBlade(), resultRate, entity.getX(), entity.getY(),
                            entity.getZ());
                }
            });
        } catch (IllegalStateException e) {
            // 注册表不存在时，跳过掉落逻辑，避免崩溃
            SlashBlade.LOGGER.warn("Failed to get registry for blade drops, skipping drop logic");
        }
    }

    public static void dropBlade(LivingEntity entity, EntityType<?> type, ItemStack blade, float percent, double x,
                                 double y, double z) {
        if (entity.getType().equals(type)) {
            var rand = entity.getLevel().getRandom();

            if (rand.nextFloat() > percent) {
                return;
            }
            ItemEntity itementity = new ItemEntity(entity.getLevel(), x, y, z, blade);
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, entity.getLevel());

            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);

            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);

            e.setAirSupply(-1);

            e.setThrower(entity.getUUID());

            entity.getLevel().addFreshEntity(e);
        }
    }
}






