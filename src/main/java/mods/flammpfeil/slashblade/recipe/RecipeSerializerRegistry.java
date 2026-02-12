package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(ForgeRegistries.RECIPE_TYPES, SlashBlade.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, SlashBlade.MODID);

    public static final RegistryObject<RecipeSerializer<?>> SLASHBLADE_SHAPED = RECIPE_SERIALIZER
            .register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);

    // 1.19.2中移除了ProudsoulShapelessRecipe，因此注释掉这个变量
    // public static final RegistryObject<RecipeSerializer<?>> PROUDSOUL_RECIPE = RECIPE_SERIALIZER
    //         .register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<?>> SLASHBLADE_SMITHING = RECIPE_SERIALIZER
            .register("slashblade_smithing", () -> SlashBladeSmithingRecipe.SERIALIZER);

    public static final RegistryObject<RecipeType<SlashBladeSmithingRecipe>> SMITHING = RECIPE_TYPES
            .register("smithing", () -> new RecipeType<SlashBladeSmithingRecipe>() {
                public String toString() {
                    return "slashblade:smithing";
                }
            });

}






