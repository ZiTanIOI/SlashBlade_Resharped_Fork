package mods.flammpfeil.slashblade.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlashBladeShapedRecipeBuilder implements RecipeBuilder {
    private static final Logger LOGGER = LogManager.getLogger(SlashBladeShapedRecipeBuilder.class);
    private final ResourceLocation result;
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final List<String> pattern = new ArrayList<>();
    private String group = "";
    private final Map<ResourceLocation, JsonObject> advancements = new HashMap<>();
    private boolean mirror = true;

    private SlashBladeShapedRecipeBuilder(ResourceLocation result) {
        this.result = result;
    }

    public static SlashBladeShapedRecipeBuilder shaped(ItemLike item) {
        return shaped(ForgeRegistries.ITEMS.getKey(item.asItem()));
    }

    public static SlashBladeShapedRecipeBuilder shaped(ResourceLocation result) {
        return new SlashBladeShapedRecipeBuilder(result);
    }

    public SlashBladeShapedRecipeBuilder define(char c, TagKey<Item> tag) {
        return this.define(c, Ingredient.of(tag));
    }

    public SlashBladeShapedRecipeBuilder define(char c, ItemLike item) {
        return this.define(c, Ingredient.of(item));
    }

    public SlashBladeShapedRecipeBuilder define(char c, Ingredient ingredient) {
        if (this.key.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        }
        if (c == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined!");
        }
        this.key.put(c, ingredient);
        return this;
    }

    public SlashBladeShapedRecipeBuilder pattern(String... pattern) {
        this.pattern.addAll(Arrays.asList(pattern));
        return this;
    }

    public SlashBladeShapedRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public SlashBladeShapedRecipeBuilder mirror(boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    @Override
    public SlashBladeShapedRecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionInstance) {
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
        this.validate(id);
        consumer.accept(new Result(id, this.result, this.group, this.pattern, this.key, this.advancements, this.mirror));
    }

    private void validate(ResourceLocation id) {
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
        }
        int i = this.pattern.get(0).length();
        for (int j = 1; j < this.pattern.size(); ++j) {
            if (i != this.pattern.get(j).length()) {
                throw new IllegalStateException("Pattern in recipe " + id + " is inconsistent: each row must be the same width!");
            }
        }
        Set<Character> set = new HashSet<>();
        set.add(' ');
        set.addAll(this.key.keySet());
        for (String s : this.pattern) {
            for (char c0 : s.toCharArray()) {
                if (!set.contains(c0)) {
                    throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'!");
                }
            }
        }
        if (this.result == null) {
            throw new IllegalStateException("No result is defined for shaped recipe " + id + "!");
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ResourceLocation result;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Map<ResourceLocation, JsonObject> advancements;
        private final boolean mirror;

        public Result(ResourceLocation id, ResourceLocation result, String group, List<String> pattern, Map<Character, Ingredient> key, Map<ResourceLocation, JsonObject> advancements, boolean mirror) {
            this.id = id;
            this.result = result;
            this.group = group;
            this.pattern = pattern;
            this.key = key;
            this.advancements = advancements;
            this.mirror = mirror;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            if (this.mirror) {
                json.addProperty("mirror", true);
            }
            JsonObject jsonobject = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", jsonobject);
            JsonArray patternArray = new JsonArray();
            for (String p : this.pattern) {
                patternArray.add(new JsonPrimitive(p));
            }
            json.add("pattern", patternArray);
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("id", this.result.toString());
            json.add("result", jsonobject1);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            // 使用自定义的shaped_blade序列化器
            return RecipeSerializerRegistry.SLASHBLADE_SHAPED.get();
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
            jsonobject.add("requirements", new JsonArray());
            return jsonobject;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancements.isEmpty() ? null : new ResourceLocation(this.id.getNamespace(), "recipes/" + this.id.getPath());
        }
    }
}