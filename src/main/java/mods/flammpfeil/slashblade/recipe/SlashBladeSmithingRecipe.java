package mods.flammpfeil.slashblade.recipe;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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

import java.util.HashMap;
import java.util.Map;
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
        try {
            // 直接创建带有正确NBT标签的刀
            ItemStack result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
            
            if (result.getItem() instanceof ItemSlashBlade) {
                // 获取刀的状态
                result.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
                    // 设置正确的translationKey
                    String translationKey = "item." + outputBlade.getNamespace() + "." + outputBlade.getPath();
                    state.setTranslationKey(translationKey);
                    
                    // 设置模型和纹理
                    String modelName = "model/named/" + outputBlade.getPath() + ".obj";
                    String textureName = "model/named/" + outputBlade.getPath() + ".png";
                    state.setModel(SlashBlade.prefix(modelName));
                    state.setTexture(SlashBlade.prefix(textureName));
                    
                    // 设置其他默认属性
                    state.setBaseAttackModifier(5.0F);
                    state.setMaxDamage(60);
                    
                    // 保存状态到NBT
                    result.getOrCreateTag().put("bladeState", state.serializeNBT());
                });
            }
            
            return result;
        } catch (Exception e) {
            // 避免崩溃
            SlashBlade.LOGGER.warn("Error getting result blade for {}: {}", outputBlade, e.getMessage());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        ItemStack result = SlashBladeSmithingRecipe.getResultBlade(this.getOutputBlade());
        return result;
    }

    @Override
    public boolean matches(Container container, @NotNull Level level) {
        return container.getContainerSize() >= 3 && this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container) {
        try {
            // 首先使用getResultItem()获取带有正确NBT标签的刀
            var result = this.getResultItem().copy();
            if (result.isEmpty()) {
                return ItemStack.EMPTY;
            }
            
            // 确保结果是拔刀剑
            if (!(result.getItem() instanceof ItemSlashBlade)) {
                return ItemStack.EMPTY;
            }

            var resultStateOpt = result.getCapability(ItemSlashBlade.BLADESTATE);
            if (!resultStateOpt.isPresent()) {
                return ItemStack.EMPTY;
            }
            var resultState = resultStateOpt.orElseThrow(() -> new IllegalStateException("Blade state not present"));
            
            // 获取基础刀
            var stack = container.getItem(1);
            var ingredientStateOpt = stack.getCapability(ItemSlashBlade.BLADESTATE);
            if (!ingredientStateOpt.isPresent()) {
                return result; // 如果基础刀没有状态，直接返回结果
            }
            var ingredientState = ingredientStateOpt.orElseThrow(() -> new IllegalStateException("Blade state not present"));

            // 合并属性
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
        } catch (Exception e) {
            // 避免在JEI预览时崩溃
            return ItemStack.EMPTY;
        }
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
        return RecipeSerializerRegistry.SMITHING.get();
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    private void updateEnchantment(ItemStack result, ItemStack ingredient) {
        // �?.19.2中，getAllEnchantments方法可能不可用，所以我们需要使用EnchantmentHelper来获取和设置附魔
        Map<Enchantment, Integer> newItemEnchants = new HashMap<>(EnchantmentHelper.getEnchantments(result));
        Map<Enchantment, Integer> oldItemEnchants = EnchantmentHelper.getEnchantments(ingredient);
        
        for (Map.Entry<Enchantment, Integer> entry : oldItemEnchants.entrySet()) {
            Enchantment enchantIndex = entry.getKey();
            int srcLevel = entry.getValue();
            int destLevel = newItemEnchants.getOrDefault(enchantIndex, 0);

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
            try {
                Ingredient ingredient = Ingredient.fromJson(json.get("template"));
                Ingredient ingredient1 = Ingredient.fromJson(json.get("base"));
                Ingredient ingredient2 = Ingredient.fromJson(json.get("addition"));
                ResourceLocation output = new ResourceLocation(GsonHelper.getAsString(json, "blade"));
                return new SlashBladeSmithingRecipe(id, output, ingredient, ingredient1, ingredient2);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn("Error parsing SlashBladeSmithingRecipe {}: {}", id, e.getMessage());
                // 返回一个默认的配方，避免崩溃
                return new SlashBladeSmithingRecipe(
                        id, 
                        new ResourceLocation(SlashBlade.MODID, "slashblade"),
                        Ingredient.EMPTY,
                        Ingredient.EMPTY,
                        Ingredient.EMPTY
                );
            }
        }

        @Override
        public SlashBladeSmithingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buffer) {
            try {
                Ingredient ingredient = Ingredient.fromNetwork(buffer);
                Ingredient ingredient1 = Ingredient.fromNetwork(buffer);
                Ingredient ingredient2 = Ingredient.fromNetwork(buffer);
                ResourceLocation blade = buffer.readResourceLocation();
                return new SlashBladeSmithingRecipe(id, blade, ingredient, ingredient1, ingredient2);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn("Error reading SlashBladeSmithingRecipe from network {}: {}", id, e.getMessage());
                // 返回一个默认的配方，避免崩溃
                return new SlashBladeSmithingRecipe(
                        id, 
                        new ResourceLocation(SlashBlade.MODID, "slashblade"),
                        Ingredient.EMPTY,
                        Ingredient.EMPTY,
                        Ingredient.EMPTY
                );
            }
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, SlashBladeSmithingRecipe recipe) {
            try {
                recipe.template.toNetwork(buffer);
                recipe.base.toNetwork(buffer);
                recipe.addition.toNetwork(buffer);
                buffer.writeResourceLocation(recipe.outputBlade);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn("Error writing SlashBladeSmithingRecipe to network: {}", e.getMessage());
                // 写入默认值，避免崩溃
                Ingredient.EMPTY.toNetwork(buffer);
                Ingredient.EMPTY.toNetwork(buffer);
                Ingredient.EMPTY.toNetwork(buffer);
                buffer.writeResourceLocation(new ResourceLocation(SlashBlade.MODID, "slashblade"));
            }
        }
    }
}






