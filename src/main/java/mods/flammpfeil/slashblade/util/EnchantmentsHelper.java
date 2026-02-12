package mods.flammpfeil.slashblade.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class EnchantmentsHelper {
    //判断A是否含有B的附�?
    public static boolean hasEnchantmentsMatch(ItemStack stackA, ItemStack stackB) {
        Map<Enchantment, Integer> enchantmentsB = EnchantmentHelper.getEnchantments(stackB);

        // 如果B没有附魔要求，直接返回true
        if (enchantmentsB.isEmpty()) {
            return true;
        }

        Map<Enchantment, Integer> enchantmentsA = EnchantmentHelper.getEnchantments(stackA);

        for (Map.Entry<Enchantment, Integer> entry : enchantmentsB.entrySet()) {
            Enchantment ench = entry.getKey();
            int requiredLevel = entry.getValue();

            // 检查A是否包含该附魔且等级足够
            if (!enchantmentsA.containsKey(ench) || enchantmentsA.get(ench) < requiredLevel) {
                return false;
            }
        }
        return true;
    }
}






