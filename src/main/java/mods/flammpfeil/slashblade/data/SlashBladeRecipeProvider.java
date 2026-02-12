package mods.flammpfeil.slashblade.data;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.advancement.SlashBladeItemPredicate;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipeBuilder;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SlashBladeRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SlashBladeRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_WOOD.get()).pattern("  L").pattern(" L ")
                .pattern("B  ").define('B', Items.WOODEN_SWORD).define('L', ItemTags.LOGS)
                .unlockedBy(getHasName(Items.WOODEN_SWORD), has(Items.WOODEN_SWORD)).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_BAMBOO.get()).pattern("  L").pattern(" L ").pattern("B  ")
                .define('B', SlashBladeItems.SLASHBLADE_WOOD.get()).define('L', SlashBladeItemTags.BAMBOO)
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()), has(SlashBladeItems.SLASHBLADE_WOOD.get())).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()).pattern(" EI").pattern("SBD")
                .pattern("PS ").define('B', SlashBladeItems.SLASHBLADE_BAMBOO.get()).define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.STRING).define('P', Items.PAPER).define('E', Items.EGG)
                .define('D', Tags.Items.DYES_BLACK)
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_BAMBOO.get()), has(SlashBladeItems.SLASHBLADE_BAMBOO.get())).save(consumer);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_WHITE.get()).pattern("  L").pattern(" L ").pattern("BG ")
                .define('B', SlashBladeItems.SLASHBLADE_WOOD.get()).define('L', SlashBladeItems.PROUDSOUL_INGOT.get())
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()), has(SlashBladeItems.SLASHBLADE_WOOD.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.YAMATO)
                .pattern("PPP")
                .pattern("PBP")
                .pattern("PPP")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.YAMATO).addSwordType(SwordType.BROKEN)
                                .addSwordType(SwordType.SEALED).build()))
                .define('P', SlashBladeItems.PROUDSOUL_SPHERE.get())
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_SPHERE.get()), inventoryTrigger(
                        new SlashBladeItemPredicate(
                                RequestDefinition.Builder.newInstance()
                                        .name(SlashBladeBuiltInRegistry.YAMATO).addSwordType(SwordType.BROKEN)
                                        .addSwordType(SwordType.SEALED).build()
                        )

                ))
                .save(consumer, SlashBlade.prefix("yamato_fix"));

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE.get()).pattern(" EI").pattern("PBD").pattern("SI ")
                .define('B',
                        SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_WHITE.get(),
                                RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
                .define('I', Tags.Items.INGOTS_GOLD).define('S', Tags.Items.STRING).define('P', Tags.Items.DYES_BLUE)
                .define('E', Tags.Items.RODS_BLAZE).define('D', Tags.Items.STORAGE_BLOCKS_COAL)
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WHITE.get()), has(SlashBladeItems.SLASHBLADE_WHITE.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.RUBY).pattern("DPI").pattern("PB ")
                .pattern("S  ")
                .define('B',
                        SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                                RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
                .define('I', SlashBladeItems.PROUDSOUL.get()).define('S', Tags.Items.STRING).define('P', SlashBladeItems.PROUDSOUL_INGOT.get())
                .define('D', Tags.Items.DYES_RED)
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()), has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_BLACK).pattern(" EF")
                .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT)
                .define('Q', Tags.Items.STORAGE_BLOCKS_QUARTZ).define('B', Items.BLAZE_POWDER)
                .define('S', SlashBladeItems.PROUDSOUL_CRYSTAL.get()).define('E', Tags.Items.OBSIDIAN)
                .define('F', Tags.Items.FEATHERS)
                .define('C', SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                        .name(SlashBladeBuiltInRegistry.RUBY)
                        .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.SMITE), 1)).build()))

                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()), has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_WHITE).pattern(" EF")
                .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT)
                .define('Q', Tags.Items.STORAGE_BLOCKS_QUARTZ).define('B', Items.BLAZE_POWDER)
                .define('S', SlashBladeItems.PROUDSOUL_CRYSTAL.get()).define('E', Tags.Items.OBSIDIAN)
                .define('F', Tags.Items.FEATHERS)
                .define('C',
                        SlashBladeIngredient.of(
                                RequestDefinition.Builder.newInstance().name(SlashBladeBuiltInRegistry.RUBY)

                                        .addEnchantment(new EnchantmentDefinition(
                                                getEnchantmentID(Enchantments.MOB_LOOTING), 1))
                                        .build()))

                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()), has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
                .save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.MURAMASA).pattern("SSS")
                .pattern("SBS").pattern("SSS")
                .define('B',
                        SlashBladeIngredient
                                .of(RequestDefinition.Builder.newInstance().proudSoul(10000).refineCount(20).build()))
                .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE.get()), inventoryTrigger(
                        new SlashBladeItemPredicate(
                                RequestDefinition.Builder.newInstance().build()
                        )

                )).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TAGAYASAN).pattern("SES")
                .pattern("DBD").pattern("SES")
                .define('B',
                        SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_WOOD.get(), RequestDefinition.Builder.newInstance()
                                .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 1))
                                .proudSoul(1000).refineCount(10).build()))
                .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get())).define('E', Ingredient.of(Items.ENDER_EYE))
                .define('D', Ingredient.of(Items.ENDER_PEARL))
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()), has(SlashBladeItems.SLASHBLADE_WOOD.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.AGITO).pattern(" S ").pattern("SBS")
                .pattern(" S ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.AGITO_RUST).killCount(100).build()))
                .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL.get()))
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL.get()), has(SlashBladeItems.PROUDSOUL.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED).pattern(" S ")
                .pattern("SBS").pattern(" S ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.OROTIAGITO_RUST).killCount(100).build()))
                .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL.get()))
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL.get()), has(SlashBladeItems.PROUDSOUL.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO).pattern("PSP")
                .pattern("SBS").pattern("PSP")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED).killCount(1000)
                                .proudSoul(1000).refineCount(10).build()))
                .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL.get())).define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_SPHERE.get()), has(SlashBladeItems.PROUDSOUL_SPHERE.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.DOUTANUKI).pattern("  P")
                .pattern(" B ").pattern("P  ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.SABIGATANA).killCount(100).proudSoul(1000)
                                .refineCount(10).build()))
                .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_SPHERE.get()), has(SlashBladeItems.PROUDSOUL_SPHERE.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.SABIGATANA).pattern("  P")
                .pattern(" P ").pattern("B  ")
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .name(SlashBladeBuiltInRegistry.SABIGATANA).addSwordType(SwordType.BROKEN)
                                .addSwordType(SwordType.SEALED).build()))
                .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_INGOT.get()))
                .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_INGOT.get()), has(SlashBladeItems.PROUDSOUL_INGOT.get())).save(consumer);

        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TUKUMO).pattern("ESD").pattern("RBL")
                .pattern("ISG").define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .define('L', Tags.Items.STORAGE_BLOCKS_LAPIS).define('G', Tags.Items.STORAGE_BLOCKS_GOLD)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON).define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('E', Tags.Items.STORAGE_BLOCKS_EMERALD)
                .define('B',
                        SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                                .addEnchantment(
                                        new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 1))
                                .build()))
                .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE.get()), inventoryTrigger(
                        new SlashBladeItemPredicate(
                                RequestDefinition.Builder.newInstance().build()
                        )

                )).save(consumer);

        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_WOODEN, Items.WOODEN_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_STONE, Items.STONE_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_IRON, Items.IRON_SWORD, consumer);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_GOLDEN, Items.GOLDEN_SWORD, consumer);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_DIAMOND, Items.DIAMOND_SWORD, consumer);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_NETHERITE, Items.NETHERITE_SWORD, consumer);
        
        // 添加启程书的无序合成配方
        ShapelessRecipeBuilder.shapeless(SlashBladeItems.BOOK_JOURNEY.get())
                .requires(Items.WOODEN_SWORD)
                .unlockedBy(getHasName(Items.WOODEN_SWORD), has(Items.WOODEN_SWORD))
                .save(consumer, SlashBlade.prefix("book_journey"));
    }

    private void rodaiRecipe(ResourceLocation rodai, ItemLike sword, Consumer<FinishedRecipe> consumer) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                        SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                                RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
                .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRING))
                .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_CRYSTAL.get()))
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()), has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
                .save(consumer);
    }

    private void rodaiAdvRecipe(ResourceLocation rodai, ItemLike sword, Consumer<FinishedRecipe> consumer) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                        SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                                RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
                .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRING))
                .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get()))
                .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()), has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
                .save(consumer);
    }

    private static ResourceLocation getEnchantmentID(Enchantment enchantment) {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
    }
}