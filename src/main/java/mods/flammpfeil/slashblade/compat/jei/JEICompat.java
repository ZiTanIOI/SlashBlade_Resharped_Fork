package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEICompat implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SlashBlade.prefix(SlashBlade.MODID);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (SlashBladeItems.SLASHBLADE.isPresent()) {
            registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE.get(), JEICompat::syncSlashBlade);
        }
        if (SlashBladeItems.SLASHBLADE_WOOD.isPresent()) {
            registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE_WOOD.get(), JEICompat::syncSlashBlade);
        }
        if (SlashBladeItems.SLASHBLADE_BAMBOO.isPresent()) {
            registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE_BAMBOO.get(), JEICompat::syncSlashBlade);
        }
        if (SlashBladeItems.SLASHBLADE_SILVERBAMBOO.isPresent()) {
            registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), JEICompat::syncSlashBlade);
        }
        if (SlashBladeItems.SLASHBLADE_WHITE.isPresent()) {
            registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE_WHITE.get(), JEICompat::syncSlashBlade);
        }
    }

    public static String syncSlashBlade(ItemStack stack, UidContext context) {
        if (stack.hasTag()) {
            var stateTag = stack.getTagElement("bladeState");
            if (stateTag != null) {
                String translationKey = stateTag.getString("translationKey");
                if (!translationKey.isBlank()) {
                    return translationKey;
                }
            }
        }

        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return "";
        }
        return "item." + key.getNamespace() + "." + key.getPath();
    }
}
