package mods.flammpfeil.slashblade.data.tag;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class SlashBladeEntityTypeTagProvider extends EntityTypeTagsProvider {

    public SlashBladeEntityTypeTagProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // 使用直接的ResourceLocation代替SlashBladeEntityTypeTags.BLACKLIST
        tag(net.minecraft.tags.TagKey.create(net.minecraft.core.Registry.ENTITY_TYPE_REGISTRY, SlashBlade.prefix("blacklist"))).add(
                EntityType.ARMOR_STAND,
                EntityType.ITEM,
                EntityType.LEASH_KNOT,
                EntityType.LIGHTNING_BOLT,
                EntityType.EXPERIENCE_BOTTLE,
                EntityType.EXPERIENCE_ORB,
                EntityType.FIREBALL,
                EntityType.SMALL_FIREBALL,
                EntityType.WITHER_SKULL,
                EntityType.DRAGON_FIREBALL,
                EntityType.ENDER_PEARL,
                EntityType.EYE_OF_ENDER,
                EntityType.SNOWBALL,
                EntityType.EGG,
                EntityType.POTION,
                EntityType.TRIDENT,
                EntityType.ARROW,
                EntityType.SPECTRAL_ARROW,
                EntityType.FISHING_BOBBER,
                EntityType.MINECART,
                EntityType.CHEST_MINECART,
                EntityType.FURNACE_MINECART,
                EntityType.TNT_MINECART,
                EntityType.HOPPER_MINECART,
                EntityType.COMMAND_BLOCK_MINECART,
                EntityType.SPAWNER_MINECART,
                EntityType.BOAT,
                EntityType.FALLING_BLOCK,
                EntityType.AREA_EFFECT_CLOUD,
                EntityType.LLAMA_SPIT,
                EntityType.PAINTING
        );
    }

    @Override
    @NotNull
    public String getName() {
        return "SlashBlade Entity Type Tags";
    }
}