package mods.flammpfeil.slashblade.data;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.data.tag.SlashBladeEntityTypeTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen {
    @SubscribeEvent
    public static void dataGen(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        dataGenerator.addProvider(event.includeServer(), new SlashBladeRecipeProvider(dataGenerator));
        dataGenerator.addProvider(event.includeServer(),
                new SlashBladeEntityTypeTagProvider(dataGenerator, SlashBlade.MODID, event.getExistingFileHelper()));
    }
}