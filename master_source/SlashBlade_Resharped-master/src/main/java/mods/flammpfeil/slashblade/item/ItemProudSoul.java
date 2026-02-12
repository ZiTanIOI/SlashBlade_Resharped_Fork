package mods.flammpfeil.slashblade.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemProudSoul extends Item {
    public ItemProudSoul(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack item) {
        return true;
    }
}
