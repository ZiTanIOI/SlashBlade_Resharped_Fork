package mods.flammpfeil.slashblade.thetwilightforest.mixin;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnchantmentsHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.inventory.UncraftingMenu;

@Mixin(UncraftingMenu.class)
public class UncraftingMenuMixin {
    @Inject(method = "matches", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onMatches(ItemStack input, ItemStack output, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        // 提前排除非拔刀剑类的情�?
        if (!(input.getItem() instanceof ItemSlashBlade) || !(output.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        // 获取刀状态（若缺失能力则直接抛出异常�?
        var inputState = input.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
        var outputState = output.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

        // 判断是否为同一类刀
        if (!inputState.getTranslationKey().equals(outputState.getTranslationKey())) {
            cir.setReturnValue(false);
            return;
        }

        // 判断断刀状态是否一�?
        if (inputState.isBroken() != outputState.isBroken()) {
            cir.setReturnValue(false);
            return;
        }

        // 判断附魔是否一�?
        if (!EnchantmentsHelper.hasEnchantmentsMatch(input, output)) {
            cir.setReturnValue(false);
        }

        // 所有条件满足时不做更改保持原true返回�?
    }
}






