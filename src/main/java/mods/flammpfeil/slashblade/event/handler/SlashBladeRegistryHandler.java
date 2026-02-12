package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SlashBlade.MODID)
public class SlashBladeRegistryHandler {

    // 用于在注册表未准备好时缓存刀的定义
    private static final Map<ResourceKey<SlashBladeDefinition>, SlashBladeDefinition> bladeDefinitionsCache = new HashMap<>();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new SlashBladeRegistryHandler());
    }

    @SubscribeEvent
    public void onRegisterBlade(SlashBladeRegistryEvent.Register event) {
        ResourceKey<SlashBladeDefinition> key = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, event.getKey());
        SlashBladeDefinition definition = event.getSlashBladeDefinition();

        // 尝试获取注册表并注册刀的定义
        try {
            // 注意：这里不能直接获取注册表，因为在MOD加载阶段注册表可能还未准备好
            // 我们需要将刀的定义缓存起来，等到游戏启动后再注册
            bladeDefinitionsCache.put(key, definition);
            SlashBlade.LOGGER.info("Cached blade definition: {}", key);
        } catch (Exception e) {
            SlashBlade.LOGGER.warn("Failed to register blade definition: {}", e.getMessage());
        }
    }

    /**
     * 获取缓存的刀的定义
     */
    public static SlashBladeDefinition getCachedBladeDefinition(ResourceKey<SlashBladeDefinition> key) {
        return bladeDefinitionsCache.get(key);
    }

    /**
     * 获取所有缓存的刀的定义
     */
    public static Map<ResourceKey<SlashBladeDefinition>, SlashBladeDefinition> getCachedBladeDefinitions() {
        return bladeDefinitionsCache;
    }
}