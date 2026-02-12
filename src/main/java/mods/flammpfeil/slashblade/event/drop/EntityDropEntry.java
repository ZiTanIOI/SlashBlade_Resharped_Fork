package mods.flammpfeil.slashblade.event.drop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate,
                              boolean requestSlashBladeKill, boolean dropFixedPoint, Vec3 dropPoint) {
    public static final Codec<EntityDropEntry> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("entity_type").forGetter(EntityDropEntry::entityType),
                    ResourceLocation.CODEC.fieldOf("blade").forGetter(EntityDropEntry::bladeName),
                    Codec.FLOAT.optionalFieldOf("drop_rate", 1.0F).forGetter(EntityDropEntry::dropRate),
                    Codec.BOOL.optionalFieldOf("request_slashblade", false)
                            .forGetter(EntityDropEntry::requestSlashBladeKill),
                    Codec.BOOL.optionalFieldOf("drop_fixed", false).forGetter(EntityDropEntry::dropFixedPoint),
                    Vec3.CODEC.optionalFieldOf("drop_point", new Vec3(0, 0, 0)).forGetter(EntityDropEntry::dropPoint)

            ).apply(instance, EntityDropEntry::new));

    public static final ResourceKey<Registry<EntityDropEntry>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(SlashBlade.prefix("entity_drop"));

    public EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate) {
        this(entityType, bladeName, dropRate, true, false, new Vec3(0, 0, 0));
    }

    public EntityDropEntry(ResourceLocation entityType, ResourceLocation bladeName, float dropRate, boolean request) {
        this(entityType, bladeName, dropRate, request, false, new Vec3(0, 0, 0));
    }

}






