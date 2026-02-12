package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.client.renderer.gui.SlashBladeBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemSlashBladeBook extends BookItem {
    public ItemSlashBladeBook(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            // 在客户端打开GUI
            Minecraft.getInstance().setScreen(new SlashBladeBookScreen());
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal("启程");
    }
    
    // 确保使用原版书的模型和纹理
    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}
