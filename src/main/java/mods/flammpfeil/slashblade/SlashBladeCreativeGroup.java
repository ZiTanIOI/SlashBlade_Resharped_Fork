package mods.flammpfeil.slashblade;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

// 1.19.2版本的创造模式标签实现
public class SlashBladeCreativeGroup {
    // 在1.19.2中，我们使用简单的方式实现创造模式标签
    public static final CreativeModeTab TAB = new CreativeModeTab(12, SlashBlade.MODID) {
        @Override
        public ItemStack makeIcon() {
            // 添加空检查，确保SLASHBLADE已经注册
            if (SlashBladeItems.SLASHBLADE.isPresent()) {
                ItemStack stack = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                    s.setNonEmpty();
                    s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                    s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                    // 手动保存状态到ItemStack的tag中
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.put("bladeState", s.serializeNBT());
                });
                return stack;
            } else {
                // 如果SLASHBLADE还未注册，返回一个默认物品
                return new ItemStack(SlashBladeItems.PROUDSOUL.get());
            }
        }

        // 1.19.2版本的方法签名
        public void fillItemList(List<ItemStack> items) {
            // 添加普通物品
            if (SlashBladeItems.PROUDSOUL.isPresent()) items.add(SlashBladeItems.PROUDSOUL.get().getDefaultInstance());
            if (SlashBladeItems.PROUDSOUL_TINY.isPresent()) items.add(SlashBladeItems.PROUDSOUL_TINY.get().getDefaultInstance());
            if (SlashBladeItems.PROUDSOUL_INGOT.isPresent()) items.add(SlashBladeItems.PROUDSOUL_INGOT.get().getDefaultInstance());
            if (SlashBladeItems.PROUDSOUL_SPHERE.isPresent()) items.add(SlashBladeItems.PROUDSOUL_SPHERE.get().getDefaultInstance());
            if (SlashBladeItems.PROUDSOUL_CRYSTAL.isPresent()) items.add(SlashBladeItems.PROUDSOUL_CRYSTAL.get().getDefaultInstance());
            if (SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.isPresent()) items.add(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get().getDefaultInstance());
            
            // 添加附魔灵魂
            fillEnchantmentsSouls(items);
            // 添加SA球体
            fillSASpheres(items);
            
            // 添加刀架
            if (SlashBladeItems.BLADESTAND_1.isPresent()) items.add(SlashBladeItems.BLADESTAND_1.get().getDefaultInstance());
            if (SlashBladeItems.BLADESTAND_1_W.isPresent()) items.add(SlashBladeItems.BLADESTAND_1_W.get().getDefaultInstance());
            if (SlashBladeItems.BLADESTAND_2.isPresent()) items.add(SlashBladeItems.BLADESTAND_2.get().getDefaultInstance());
            if (SlashBladeItems.BLADESTAND_2_W.isPresent()) items.add(SlashBladeItems.BLADESTAND_2_W.get().getDefaultInstance());
            if (SlashBladeItems.BLADESTAND_S.isPresent()) items.add(SlashBladeItems.BLADESTAND_S.get().getDefaultInstance());
            if (SlashBladeItems.BLADESTAND_V.isPresent()) items.add(SlashBladeItems.BLADESTAND_V.get().getDefaultInstance());
            
            // 添加基础刀
            if (SlashBladeItems.SLASHBLADE_WOOD.isPresent()) items.add(SlashBladeItems.SLASHBLADE_WOOD.get().getDefaultInstance());
            if (SlashBladeItems.SLASHBLADE_BAMBOO.isPresent()) items.add(SlashBladeItems.SLASHBLADE_BAMBOO.get().getDefaultInstance());
            if (SlashBladeItems.SLASHBLADE_SILVERBAMBOO.isPresent()) items.add(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get().getDefaultInstance());
            if (SlashBladeItems.SLASHBLADE_WHITE.isPresent()) items.add(SlashBladeItems.SLASHBLADE_WHITE.get().getDefaultInstance());
            if (SlashBladeItems.SLASHBLADE.isPresent()) items.add(SlashBladeItems.SLASHBLADE.get().getDefaultInstance());
            
            // 添加所有命名刀
            fillBlades(items);
        }
    };
    
    // 为了向后兼容，提供一个直接访问创造模式标签的方式
    public static CreativeModeTab getTab() {
        return TAB;
    }

    private static void fillEnchantmentsSouls(List<ItemStack> items) {
        // 添加空检查，确保SLASHBLADE和PROUDSOUL_TINY已经注册
        if (SlashBladeItems.SLASHBLADE.isPresent() && SlashBladeItems.PROUDSOUL_TINY.isPresent()) {
            ForgeRegistries.ENCHANTMENTS.forEach(enchantment -> {
                ItemStack blade = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                if (blade.canApplyAtEnchantingTable(enchantment)) {
                    ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                    soul.enchant(enchantment, 1);
                    items.add(soul);
                }
            });
        }
    }

    private static void fillSASpheres(List<ItemStack> items) {
        // 添加空检查，确保PROUDSOUL_SPHERE已经注册
        if (SlashBladeItems.PROUDSOUL_SPHERE.isPresent()) {
            SlashArtsRegistry.REGISTRY.get().forEach(slashArts -> {
                ResourceLocation key = SlashArtsRegistry.REGISTRY.get().getKey(slashArts);
                if (slashArts.equals(SlashArtsRegistry.NONE.get()) || key == null) {
                    return;
                }
                ItemStack sphere = new ItemStack(SlashBladeItems.PROUDSOUL_SPHERE.get());
                CompoundTag tag = new CompoundTag();
                tag.putString("SpecialAttackType", key.toString());
                sphere.setTag(tag);
                items.add(sphere);
            });
        }
    }
    
    private static void fillBlades(List<ItemStack> items) {
        // 无论注册表状态如何，总是添加基础刀和所有与"无铭"共用代码的刀
        // 这样可以确保所有刀都显示在创造模式物品栏中
        
        // 添加一些基础刀
        addBasicBlades(items);
        
        // 添加所有与"无铭"共用代码的刀
        addNamedBlades(items);
    }
    
    private static void addBasicBlades(List<ItemStack> items) {
        // 手动添加一些基础刀
        ItemStack defaultBlade = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        items.add(defaultBlade);
    }
    
    private static void addNamedBlades(List<ItemStack> items) {
        // 直接从SlashBladeBuiltInRegistry获取所有刀的定义并添加
        try {
            // 注意：这里我们不能直接获取注册的刀的定义，因为在创造模式标签初始化时注册表可能还未准备好
            // 我们需要手动创建这些刀的实例
            
            // 检查SLASHBLADE是否已经被注册
            if (!SlashBladeItems.SLASHBLADE.isPresent()) {
                SlashBlade.LOGGER.warn("SLASHBLADE not yet registered, skipping named blades");
                return;
            }
            
            // 获取SLASHBLADE实例
            Item slashBladeItem = SlashBladeItems.SLASHBLADE.get();
            if (slashBladeItem == null) {
                SlashBlade.LOGGER.warn("Failed to get SLASHBLADE instance, skipping named blades");
                return;
            }
            
            SlashBlade.LOGGER.info("Adding named blades to creative menu");
            
            // 添加鄂门
            ItemStack orotiagito = new ItemStack(slashBladeItem);
            orotiagito.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/orotiagito.png"));
                s.setBaseAttackModifier(7.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "wave_edge"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.orotiagito");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagito.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(orotiagito);
            
            // 添加村正
            ItemStack muramasa = new ItemStack(slashBladeItem);
            muramasa.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.png"));
                s.setBaseAttackModifier(7.0F);
                s.setMaxDamage(50);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_vertical"));
                s.setTranslationKey("slashblade.muramasa");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = muramasa.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(muramasa);
            
            // 添加阎魔刀
            ItemStack yamato = new ItemStack(slashBladeItem);
            yamato.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                s.setBaseAttackModifier(7.0F);
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.yamato");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yamato.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(yamato);
            
            // 添加锈刀
            ItemStack agitoRust = new ItemStack(slashBladeItem);
            agitoRust.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_rust.png"));
                s.setBaseAttackModifier(3.0F);
                s.setMaxDamage(60);
                s.setSealed(true);
                s.setTranslationKey("slashblade.agito_rust");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = agitoRust.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(agitoRust);
            
            // 添加付丧结月
            ItemStack tukumo = new ItemStack(slashBladeItem);
            tukumo.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/a_tukumo.png"));
                s.setBaseAttackModifier(6.0F);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_horizontal"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.tukumo");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = tukumo.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(tukumo);
            
            // 添加枯石大刀
            ItemStack koseki = new ItemStack(slashBladeItem);
            koseki.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/dios/dios.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/dios/koseki.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "drive_vertical"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "wither_edge"));
                s.setTranslationKey("slashblade.koseki");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = koseki.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(koseki);
            
            // 添加散华
            ItemStack sange = new ItemStack(slashBladeItem);
            sange.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "void_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.sange");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = sange.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(sange);
            
            // 添加钢剑胴田贯
            ItemStack doutanuki = new ItemStack(slashBladeItem);
            doutanuki.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/muramasa.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/muramasa/doutanuki.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "circle_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.doutanuki");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = doutanuki.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(doutanuki);
            
            // 添加夜叉
            ItemStack yasha = new ItemStack(slashBladeItem);
            yasha.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "sakura_end"));
                s.setTranslationKey("slashblade.yasha");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yasha.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(yasha);
            
            // 添加真夜叉
            ItemStack yashaTrue = new ItemStack(slashBladeItem);
            yashaTrue.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha_true.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yasha/yasha.png"));
                s.setBaseAttackModifier(6.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "circle_slash"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.yasha_true");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yashaTrue.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(yashaTrue);
            
            // 添加黑狐
            ItemStack foxBlack = new ItemStack(slashBladeItem);
            foxBlack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/black.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "piercing"));
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.fox_black");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = foxBlack.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(foxBlack);
            
            // 添加白狐
            ItemStack foxWhite = new ItemStack(slashBladeItem);
            foxWhite.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/sange/sange.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/sange/white.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(70);
                s.addSpecialEffect(new ResourceLocation(SlashBlade.MODID, "bewitched"));
                s.setTranslationKey("slashblade.fox_white");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = foxWhite.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(foxWhite);
            
            // 添加鄂门（封）
            ItemStack orotiagitoSealed = new ItemStack(slashBladeItem);
            orotiagitoSealed.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_true.png"));
                s.setBaseAttackModifier(5.0F);
                s.setMaxDamage(60);
                s.setSlashArtsKey(new ResourceLocation(SlashBlade.MODID, "wave_edge"));
                s.setSealed(true);
                s.setTranslationKey("slashblade.orotiagito_sealed");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagitoSealed.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(orotiagitoSealed);
            
            // 添加鄂门（锈）
            ItemStack orotiagitoRust = new ItemStack(slashBladeItem);
            orotiagitoRust.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/agito.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/agito_rust_true.png"));
                s.setBaseAttackModifier(3.0F);
                s.setMaxDamage(60);
                s.setSealed(true);
                s.setTranslationKey("slashblade.orotiagito_rust");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = orotiagitoRust.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(orotiagitoRust);
            
            // 添加断阎魔刀
            ItemStack yamatoBroken = new ItemStack(slashBladeItem);
            yamatoBroken.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setNonEmpty();
                s.setModel(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.obj"));
                s.setTexture(new ResourceLocation(SlashBlade.MODID, "model/named/yamato.png"));
                s.setBaseAttackModifier(7.0F);
                s.setBroken(true);
                s.setSealed(true);
                s.setTranslationKey("slashblade.yamato_broken");
                // 手动保存状态到ItemStack的tag中
                CompoundTag tag = yamatoBroken.getOrCreateTag();
                tag.put("bladeState", s.serializeNBT());
            });
            items.add(yamatoBroken);
            
            SlashBlade.LOGGER.info("Successfully added named blades to creative menu");
        } catch (Exception e) {
            SlashBlade.LOGGER.warn("Failed to add named blades: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}