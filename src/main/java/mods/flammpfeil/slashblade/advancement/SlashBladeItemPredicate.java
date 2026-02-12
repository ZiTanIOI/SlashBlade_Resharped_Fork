package mods.flammpfeil.slashblade.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class SlashBladeItemPredicate extends ItemPredicate {

    private final RequestDefinition request;

    public SlashBladeItemPredicate(JsonObject json) {
        this(json.getAsJsonObject("requestBlade").isJsonNull()
                ? RequestDefinition.Builder.newInstance().build()
                : RequestDefinition.fromJSON(json.getAsJsonObject("requestBlade")));
    }

    @Override
    public @NotNull JsonElement serializeToJson() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", SlashBlade.prefix("slashblade").toString());
        jsonobject.add("requestBlade", this.getRequest().toJson());
        return jsonobject;
    }

    public SlashBladeItemPredicate(RequestDefinition request) {
        this.request = request;
    }

    @Override
    public boolean matches(@NotNull ItemStack stack) {
        var name = this.getRequest().name();
        boolean requestCheck = this.getRequest().test(stack);
        if (name.equals(SlashBlade.prefix("none"))) {
            return requestCheck && stack.is(SlashBladeItems.SLASHBLADE.get());
        }
        if (ForgeRegistries.ITEMS.containsKey(name)) {
            return requestCheck && stack.is(ForgeRegistries.ITEMS.getValue(name));
        }
        return requestCheck && (stack.getItem() instanceof ItemSlashBlade);
    }

    public RequestDefinition getRequest() {
        return request;
    }
}






