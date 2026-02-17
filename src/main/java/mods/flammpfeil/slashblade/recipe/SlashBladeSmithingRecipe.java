package mods.flammpfeil.slashblade.recipe;

import com.google.gson.JsonObject;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.event.handler.SlashBladeRegistryHandler;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public class SlashBladeSmithingRecipe implements Recipe<Container> {
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public static final RecipeSerializer<SlashBladeSmithingRecipe> SERIALIZER = new SlashBladeSmithingRecipe.Serializer();
    public static RecipeType<SlashBladeSmithingRecipe> TYPE;

    public static void setType(RecipeType<SlashBladeSmithingRecipe> type) {
        TYPE = type;
    }

    private final ResourceLocation outputBlade;
    private final ResourceLocation id;

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public SlashBladeSmithingRecipe(ResourceLocation id, ResourceLocation outputBlade, Ingredient template, Ingredient base, Ingredient addition) {
        super();
        this.id = id;
        this.outputBlade = outputBlade;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    public SlashBladeSmithingRecipe(ResourceLocation outputBlade, Ingredient template, Ingredient base, Ingredient addition) {
        this(outputBlade, outputBlade, template, base, addition);
    }

    public Ingredient getTemplate() {
        return template;
    }

    public Ingredient getBase() {
        return base;
    }

    public Ingredient getAddition() {
        return addition;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = ForgeRegistries.ITEMS.containsKey(outputBlade) ? ForgeRegistries.ITEMS.getValue(outputBlade)
                : SlashBladeItems.SLASHBLADE.get();

        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }

    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, outputBlade);
    }

    private ItemStack resolveOutputBlade() {
        ItemStack result = getResultBlade(this.getOutputBlade());
        if (!Objects.equals(ForgeRegistries.ITEMS.getKey(result.getItem()), getOutputBlade())) {
            SlashBladeDefinition definition = SlashBladeRegistryHandler.getCachedBladeDefinition(getOutputBladeKey());
            if (definition != null) {
                result = definition.getBlade();
            } else {
                result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                final ItemStack finalResult = result;
                finalResult.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
                    state.setNonEmpty();
                    state.setTranslationKey("item." + outputBlade.getNamespace() + "." + outputBlade.getPath());
                    finalResult.getOrCreateTag().put("bladeState", state.serializeNBT());
                });
            }
        }
        return result;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return resolveOutputBlade();
    }

    @Override
    public boolean matches(Container container, @NotNull Level level) {
        return container.getContainerSize() >= 3
                && this.template.test(container.getItem(0))
                && this.base.test(container.getItem(1))
                && this.addition.test(container.getItem(2));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container) {
        var result = this.getResultItem();
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }

        var resultState = result.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        var stack = container.getItem(1);
        if (!(stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent())) {
            return ItemStack.EMPTY;
        }
        var ingredientState = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

        resultState.setProudSoulCount(resultState.getProudSoulCount() + ingredientState.getProudSoulCount());
        resultState.setKillCount(
                SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get() ?
                        Math.max(resultState.getKillCount(), ingredientState.getKillCount()) :
                        resultState.getKillCount() + ingredientState.getKillCount()
        );
        resultState.setRefine(resultState.getRefine() + ingredientState.getRefine());
        result.getOrCreateTag().put("bladeState", resultState.serializeNBT());
        updateEnchantment(result, stack);

        return result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SlashBladeSmithingRecipe.SERIALIZER;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(ForgeHooks::hasNoElements);
    }

    private void updateEnchantment(ItemStack result, ItemStack ingredient) {
        var newItemEnchants = result.getAllEnchantments();
        var oldItemEnchants = ingredient.getAllEnchantments();
        for (Enchantment enchantIndex : oldItemEnchants.keySet()) {

            int destLevel = newItemEnchants.getOrDefault(enchantIndex, 0);
            int srcLevel = oldItemEnchants.get(enchantIndex);

            srcLevel = Math.max(srcLevel, destLevel);
            srcLevel = Math.min(srcLevel, enchantIndex.getMaxLevel());

            boolean canApplyFlag = enchantIndex.canApplyAtEnchantingTable(result);
            if (canApplyFlag) {
                for (Enchantment curEnchantIndex : newItemEnchants.keySet()) {
                    if (curEnchantIndex != enchantIndex
                            && !enchantIndex.isCompatibleWith(curEnchantIndex)) {
                        canApplyFlag = false;
                        break;
                    }
                }
                if (canApplyFlag) {
                    newItemEnchants.put(enchantIndex, srcLevel);
                }
            }
        }
        EnchantmentHelper.setEnchantments(newItemEnchants, result);
    }

    public static class Serializer implements RecipeSerializer<SlashBladeSmithingRecipe> {
        @Override
        public @NotNull SlashBladeSmithingRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("template"));
            Ingredient ingredient1 = Ingredient.fromJson(json.get("base"));
            Ingredient ingredient2 = Ingredient.fromJson(json.get("addition"));
            ResourceLocation output = new ResourceLocation(GsonHelper.getAsString(json, "blade"));
            return new SlashBladeSmithingRecipe(id, output, ingredient, ingredient1, ingredient2);
        }

        @Override
        public SlashBladeSmithingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient ingredient1 = Ingredient.fromNetwork(buffer);
            Ingredient ingredient2 = Ingredient.fromNetwork(buffer);
            ResourceLocation blade = buffer.readResourceLocation();
            return new SlashBladeSmithingRecipe(id, blade, ingredient, ingredient1, ingredient2);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, SlashBladeSmithingRecipe recipe) {
            recipe.template.toNetwork(buffer);
            recipe.base.toNetwork(buffer);
            recipe.addition.toNetwork(buffer);
            buffer.writeResourceLocation(recipe.outputBlade);
        }
    }
}
