package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;

@EmiEntrypoint
public class EMICompat implements EmiPlugin {

    public static final EmiRecipeCategory SLASHBLADE_SMITHING_CATEGORY = new EmiRecipeCategory(
            SlashBlade.prefix("slashblade_smithing"),
            EmiStack.of(Blocks.SMITHING_TABLE)
    );
    public static final EmiRecipeCategory SLASHBLADE_SHAPED_CATEGORY = new EmiRecipeCategory(
            SlashBlade.prefix("shaped_blade"),
            EmiStack.of(Blocks.CRAFTING_TABLE)
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SLASHBLADE_SMITHING_CATEGORY);
        registry.addCategory(SLASHBLADE_SHAPED_CATEGORY);

        // 注册SlashBlade锻造配�?
        HashSet<ResourceLocation> vanillaSmithing = new HashSet<>();
        List<SlashBladeSmithingRecipe> smithingRecipes = findRecipesByType(RecipeType.SMITHING).stream()
                .filter(r -> r instanceof SlashBladeSmithingRecipe).map(r -> (SlashBladeSmithingRecipe) r).toList();
        for (SlashBladeSmithingRecipe recipe : smithingRecipes) {
            registry.addRecipe(new SlashBladeSmithingEmiRecipe(recipe));
            vanillaSmithing.add(recipe.getId());
        }
        registry.removeRecipes(emiRecipe -> vanillaSmithing.contains(emiRecipe.getId()) && !(emiRecipe instanceof SlashBladeSmithingEmiRecipe));

        // 添加工作�?
        registry.addWorkstation(SLASHBLADE_SMITHING_CATEGORY, EmiStack.of(Blocks.SMITHING_TABLE));
        registry.removeRecipes(ResourceLocation.tryParse("emi:/crafting/repairing/slashblade/slashblade"));

    }

    private static <C extends Container, T extends Recipe<C>> List<T> findRecipesByType(RecipeType<T> type) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.level != null) {
            return instance.level.getRecipeManager().getAllRecipesFor(type);
        }
        return List.of();
    }
}






