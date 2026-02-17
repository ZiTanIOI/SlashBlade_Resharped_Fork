package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SlashBlade.MODID)
public class SlashBladeRegistryHandler {

    private static final Map<ResourceKey<SlashBladeDefinition>, SlashBladeDefinition> bladeDefinitionsCache = new HashMap<>();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new SlashBladeRegistryHandler());
    }

    @SubscribeEvent
    public void onRegisterBlade(SlashBladeRegistryEvent.Register event) {
        ResourceKey<SlashBladeDefinition> key = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, event.getKey());
        cacheBladeDefinition(key, event.getSlashBladeDefinition());
    }

    public static void cacheBladeDefinition(ResourceKey<SlashBladeDefinition> key, SlashBladeDefinition definition) {
        bladeDefinitionsCache.put(key, definition);
    }

    public static SlashBladeDefinition getCachedBladeDefinition(ResourceKey<SlashBladeDefinition> key) {
        return bladeDefinitionsCache.get(key);
    }

    public static Map<ResourceKey<SlashBladeDefinition>, SlashBladeDefinition> getCachedBladeDefinitions() {
        return bladeDefinitionsCache;
    }
}
