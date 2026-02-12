package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerSerializers(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> CraftingHelper
                .register(SlashBlade.prefix("blade"), SlashBladeIngredient.Serializer.INSTANCE));
    }
}