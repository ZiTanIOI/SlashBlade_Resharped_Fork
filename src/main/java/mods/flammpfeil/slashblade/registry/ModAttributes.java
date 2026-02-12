package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, SlashBlade.MODID);

    public static final RegistryObject<Attribute> SLASHBLADE_DAMAGE = ATTRIBUTES.register("slashblade_damage",
            () -> new RangedAttribute("attribute.name.generic.slashblade_damage", 1.0d, 0.0d, 512.0d).setSyncable(true));


    public static Attribute getSlashBladeDamage() {
        return SLASHBLADE_DAMAGE.get();
    }

}






