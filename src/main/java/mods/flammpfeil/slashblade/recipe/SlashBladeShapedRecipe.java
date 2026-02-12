package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlashBladeShapedRecipe extends ShapedRecipe {

    public static final RecipeSerializer<SlashBladeShapedRecipe> SERIALIZER = new SlashBladeShapedRecipeSerializer<>(
            RecipeSerializer.SHAPED_RECIPE, SlashBladeShapedRecipe::new);

    private final ResourceLocation outputBlade;

    public SlashBladeShapedRecipe(ShapedRecipe compose, ResourceLocation outputBlade) {
        super(compose.getId(), compose.getGroup(), compose.getWidth(), compose.getHeight(),
                compose.getIngredients(), getResultBlade(outputBlade));
        this.outputBlade = outputBlade;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = ForgeRegistries.ITEMS.containsKey(outputBlade) ? ForgeRegistries.ITEMS.getValue(outputBlade)
                : SlashBladeItems.SLASHBLADE.get();

        ItemStack result = Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
        
        // 初始化NBT标签
        if (result.getItem() instanceof ItemSlashBlade) {
            result.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
                // 设置正确的translationKey
                String translationKey = "item." + outputBlade.getNamespace() + "." + outputBlade.getPath();
                state.setTranslationKey(translationKey);
                
                // 设置模型和纹理
                String modelName = "model/named/" + outputBlade.getPath() + ".obj";
                String textureName = "model/named/" + outputBlade.getPath() + ".png";
                state.setModel(mods.flammpfeil.slashblade.SlashBlade.prefix(modelName));
                state.setTexture(mods.flammpfeil.slashblade.SlashBlade.prefix(textureName));
                
                // 设置其他默认属性
                state.setBaseAttackModifier(5.0F);
                state.setMaxDamage(60);
                
                // 保存状态到NBT
                result.getOrCreateTag().put("bladeState", state.serializeNBT());
            });
        }
        
        return result;
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, outputBlade);
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return SlashBladeShapedRecipe.getResultBlade(this.getOutputBlade());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container) {
        var result = this.getResultItem();
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }

        var resultState = result.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        boolean sumRefine = SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get();
        int proudSoul = resultState.getProudSoulCount();
        int killCount = resultState.getKillCount();
        int refine = resultState.getRefine();
        for (int i = 0; i < container.getContainerSize(); i++) {
            var stack = container.getItem(i);
            if (!(stack.getItem() instanceof ItemSlashBlade)) {
                continue;
            }
            var ingredientState = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

            proudSoul += ingredientState.getProudSoulCount();
            killCount += ingredientState.getKillCount();
            if (sumRefine) {
                refine += ingredientState.getRefine();
            } else {
                refine = Math.max(refine, ingredientState.getRefine());
            }
            updateEnchantment(result, stack);
        }
        resultState.setProudSoulCount(proudSoul);
        resultState.setKillCount(killCount);
        resultState.setRefine(refine);
        result.getOrCreateTag().put("bladeState", resultState.serializeNBT());

        return result;
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
                            && !enchantIndex.isCompatibleWith(curEnchantIndex) /* canApplyTogether */) {
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

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

}






