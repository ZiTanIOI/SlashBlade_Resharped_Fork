package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
    }

    public static String syncSlashBlade(ItemStack stack, UidContext context) {
        // 同步nbt到Cap
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(cap -> {
            if (stack.getOrCreateTag().contains("bladeState")) {
                cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
            }
        });

        // 为无铭「无名」设置默认值
        return stack.getCapability(ItemSlashBlade.BLADESTATE).map(state -> {
            String translationKey = state.getTranslationKey();
            // 如果translationKey为空，使用默认值
            if (translationKey.isEmpty() || translationKey.equals("item.minecraft.air")) {
                return "item.slashblade.slashblade";
            }
            return translationKey;
        }).orElse("");
    }

}
