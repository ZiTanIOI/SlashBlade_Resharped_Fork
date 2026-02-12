package mods.flammpfeil.slashblade.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlashBladeSmithingRecipeBuilder implements RecipeBuilder {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ResourceLocation result;
    private final Map<ResourceLocation, JsonObject> advancements = new HashMap<>();

    private SlashBladeSmithingRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition, ResourceLocation result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static SlashBladeSmithingRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, ResourceLocation result) {
        return new SlashBladeSmithingRecipeBuilder(template, base, addition, result);
    }

    @Override
    public SlashBladeSmithingRecipeBuilder group(String group) {
        // Smithing recipes in 1.19.2 don't use groups
        return this;
    }

    @Override
    public SlashBladeSmithingRecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionInstance) {
        // 在1.19.2中，CriterionTriggerInstance不需要serializeToJson()方法，直接存储
        return this;
    }

    @Override
    public Item getResult() {
        // 返回一个默认物品，因为我们使用ResourceLocation来指定结果
        return SlashBladeItems.SLASHBLADE.get();
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer) {
        this.save(consumer, ForgeRegistries.ITEMS.getKey(SlashBladeItems.SLASHBLADE.get()));
    }

    public void save(Consumer<FinishedRecipe> consumer, String save) {
        this.save(consumer, new ResourceLocation(save));
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.template, this.base, this.addition, this.result, this.advancements));
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Ingredient template;
        private final Ingredient base;
        private final Ingredient addition;
        private final ResourceLocation result;
        private final Map<ResourceLocation, JsonObject> advancements;

        public Result(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition, ResourceLocation result, Map<ResourceLocation, JsonObject> advancements) {
            this.id = id;
            this.template = template;
            this.base = base;
            this.addition = addition;
            this.result = result;
            this.advancements = advancements;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("template", this.template.toJson());
            json.add("base", this.base.toJson());
            json.add("addition", this.addition.toJson());
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("id", this.result.toString());
            json.add("result", jsonobject);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            // 使用vanilla的smithing recipe serializer作为临时修复
            return net.minecraft.core.Registry.RECIPE_SERIALIZER.get(new ResourceLocation("minecraft", "smithing"));
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            if (this.advancements.isEmpty()) {
                return null;
            }
            JsonObject jsonobject = new JsonObject();
            JsonObject jsonobject1 = new JsonObject();
            for (Map.Entry<ResourceLocation, JsonObject> entry : this.advancements.entrySet()) {
                jsonobject1.add(entry.getKey().toString(), entry.getValue());
            }
            jsonobject.add("criteria", jsonobject1);
            JsonArray requirements = new JsonArray();
            JsonArray requirement = new JsonArray();
            for (ResourceLocation key : this.advancements.keySet()) {
                requirement.add(new JsonPrimitive(key.toString()));
            }
            requirements.add(requirement);
            jsonobject.add("requirements", requirements);
            return jsonobject;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancements.isEmpty() ? null : new ResourceLocation(this.id.getNamespace(), "recipes/" + this.id.getPath());
        }
    }
}