package mods.flammpfeil.slashblade;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SlashBladeCreativeGroup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, SlashBlade.MODID);

    private static final CreativeModeTab SLASHBLADE = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.slashblade")).icon(() -> {
                ItemStack stack = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                    s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                    s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                });
                return stack;
            }).displayItems((features, output) -> {

                output.accept(SlashBladeItems.PROUDSOUL.get());
                output.accept(SlashBladeItems.PROUDSOUL_TINY.get());
                output.accept(SlashBladeItems.PROUDSOUL_INGOT.get());
                output.accept(SlashBladeItems.PROUDSOUL_SPHERE.get());

                output.accept(SlashBladeItems.PROUDSOUL_CRYSTAL.get());
                output.accept(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get());
                fillEnchantmentsSouls(output);
                fillSASpheres(output);
                output.accept(SlashBladeItems.BLADESTAND_1.get());
                output.accept(SlashBladeItems.BLADESTAND_1_W.get());
                output.accept(SlashBladeItems.BLADESTAND_2.get());
                output.accept(SlashBladeItems.BLADESTAND_2_W.get());
                output.accept(SlashBladeItems.BLADESTAND_S.get());
                output.accept(SlashBladeItems.BLADESTAND_V.get());

                output.accept(SlashBladeItems.SLASHBLADE_WOOD.get());
                output.accept(SlashBladeItems.SLASHBLADE_BAMBOO.get());
                output.accept(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get());
                output.accept(SlashBladeItems.SLASHBLADE_WHITE.get());
                output.accept(SlashBladeItems.SLASHBLADE.get());

                //fillBlades(features, output);
            }).build();

    public static final RegistryObject<CreativeModeTab> SLASHBLADE_GROUP = CREATIVE_MODE_TABS.register("slashblade",
            () -> SLASHBLADE);

    @SuppressWarnings("unused")
    @Deprecated
    private static void fillBlades(CreativeModeTab.ItemDisplayParameters features, CreativeModeTab.Output output) {
        SlashBlade.getSlashBladeDefinitionRegistry(features.holders()).listElements()
                .sorted(SlashBladeDefinition.COMPARATOR).forEach(entry -> {
                    if (!entry.value().getBlade().isEmpty()) {
                        output.accept(entry.value().getBlade());
                    }
                });
    }

    private static void fillEnchantmentsSouls(CreativeModeTab.Output output) {
        ForgeRegistries.ENCHANTMENTS.forEach(enchantment -> {
            ItemStack blade = new ItemStack(SlashBladeItems.SLASHBLADE.get());
            if (blade.canApplyAtEnchantingTable(enchantment)) {
                ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                soul.enchant(enchantment, 1);
                output.accept(soul);
            }

        });
    }

    private static void fillSASpheres(CreativeModeTab.Output output) {
        SlashArtsRegistry.REGISTRY.get().forEach(slashArts -> {
            ResourceLocation key = SlashArtsRegistry.REGISTRY.get().getKey(slashArts);
            if (slashArts.equals(SlashArtsRegistry.NONE.get()) || key == null) {
                return;
            }
            ItemStack sphere = new ItemStack(SlashBladeItems.PROUDSOUL_SPHERE.get());
            CompoundTag tag = new CompoundTag();
            tag.putString("SpecialAttackType", key.toString());
            sphere.setTag(tag);
            output.accept(sphere);
        });
    }
}
