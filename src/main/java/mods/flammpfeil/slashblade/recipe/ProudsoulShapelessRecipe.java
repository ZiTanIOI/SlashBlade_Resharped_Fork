package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ProudsoulShapelessRecipe extends ShapelessRecipe {

    public ProudsoulShapelessRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, result, ingredients);
    }

    public static final RecipeSerializer<ProudsoulShapelessRecipe> SERIALIZER = new Serializer();

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container) {
        ItemStack result = super.assemble(container);
        Map<Enchantment, Integer> all = Maps.newHashMap();

        for (int idx = 0; idx < container.getContainerSize(); idx++) {
            ItemStack stack = container.getItem(idx);
            if (stack.isEmpty() || !stack.isEnchanted()) {
                continue;
            }
            Map<Enchantment, Integer> emap = EnchantmentHelper.getEnchantments(stack);
            all.putAll(emap);
        }

        EnchantmentHelper.setEnchantments(all, result);
        return result;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
        boolean result = super.matches(container, level);

        if (result) {
            Map<Enchantment, Integer> all = Maps.newHashMap();
            int soulCount = 0;

            for (int idx = 0; idx < container.getContainerSize(); idx++) {
                ItemStack stack = container.getItem(idx);
                if (stack.isEmpty() || !stack.isEnchanted()) {
                    continue;
                }

                soulCount++;
                Map<Enchantment, Integer> emap = EnchantmentHelper.getEnchantments(stack);

                for (Map.Entry<Enchantment, Integer> entry : emap.entrySet()) {
                    all.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }

            result = all.size() == 1 || all.isEmpty();
            if (result) {
                for (Map.Entry<Enchantment, Integer> entry : all.entrySet()) {
                    result = entry.getValue() == soulCount;
                }
            }
        }

        return result;
    }

    public static class Serializer implements RecipeSerializer<ProudsoulShapelessRecipe> {
        @Override
        public @NotNull ProudsoulShapelessRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (ingredients.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe. The maximum is 9");
            } else {
                ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new ProudsoulShapelessRecipe(id, group, itemstack, ingredients);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray array) {
            NonNullList<Ingredient> list = NonNullList.create();

            for (int i = 0; i < array.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(array.get(i));
                list.add(ingredient);
            }

            return list;
        }

        @Override
        public ProudsoulShapelessRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            return new ProudsoulShapelessRecipe(id, group, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ProudsoulShapelessRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.getResultItem());
        }
    }
}
