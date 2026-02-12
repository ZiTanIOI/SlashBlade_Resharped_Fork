package mods.flammpfeil.slashblade.item;

import com.mojang.serialization.Codec;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumSet;

public enum SwordType {
    NONE, EDGEFRAGMENT, BROKEN, ENCHANTED, BEWITCHED, FIERCEREDGE, NOSCABBARD, SEALED, UNBREAKABLE, SOULEATER;

    public static final Codec<SwordType> CODEC = Codec.STRING.xmap(string -> SwordType.valueOf(string.toUpperCase()),
            instance -> instance.name().toLowerCase());

    public static EnumSet<SwordType> from(ItemStack itemStackIn) {
        EnumSet<SwordType> types = EnumSet.noneOf(SwordType.class);

        LazyOptional<ISlashBladeState> state = itemStackIn.getCapability(ItemSlashBlade.BLADESTATE);

        if (state.isPresent()) {
            itemStackIn.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                if (s.isBroken() || getElement(itemStackIn).getBoolean("isBroken")) {
                    types.add(BROKEN);
                }

                if (s.isSealed() || getElement(itemStackIn).getBoolean("isSealed")) {
                    types.add(SEALED);
                }

                if (!s.isSealed() && itemStackIn.isEnchanted()
                        && (itemStackIn.hasCustomHoverName() || s.isDefaultBewitched())) {
                    types.add(BEWITCHED);
                }

                if (s.getKillCount() >= 1000) {
                    types.add(FIERCEREDGE);
                }

                if (s.getProudSoulCount() >= 10000) {
                    types.add(SOULEATER);
                }

            });
        } else {
            types.add(NOSCABBARD);
            types.add(EDGEFRAGMENT);
        }

        if (itemStackIn.isEnchanted()) {
            types.add(ENCHANTED);
        }

        if (itemStackIn.getItem() instanceof ItemSlashBladeDetune) {
            types.remove(SwordType.ENCHANTED);
            types.remove(SwordType.BEWITCHED);
        }

        if (itemStackIn.getOrCreateTag().getBoolean("Unbreakable")) {
            types.remove(SwordType.BROKEN);
        }
        return types;
    }

    private static CompoundTag getElement(ItemStack itemStackIn) {
        if (itemStackIn.getOrCreateTag().contains("bladeState")) {
            return itemStackIn.getTagElement("bladeState");
        }
        return new CompoundTag();
    }
}
