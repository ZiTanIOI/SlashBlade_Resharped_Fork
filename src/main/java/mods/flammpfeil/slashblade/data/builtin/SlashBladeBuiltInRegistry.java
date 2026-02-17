package mods.flammpfeil.slashblade.data.builtin;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.event.handler.SlashBladeRegistryHandler;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import mods.flammpfeil.slashblade.registry.slashblade.PropertiesDefinition;
import mods.flammpfeil.slashblade.registry.slashblade.RenderDefinition;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class SlashBladeBuiltInRegistry {
    public static final ResourceLocation YAMATO = SlashBlade.prefix("yamato");
    public static final ResourceLocation YAMATO_BROKEN = SlashBlade.prefix("yamato_broken");

    public static final ResourceLocation TUKUMO = SlashBlade.prefix("yuzukitukumo");
    public static final ResourceLocation MURAMASA = SlashBlade.prefix("muramasa");
    public static final ResourceLocation RUBY = SlashBlade.prefix("ruby");
    public static final ResourceLocation SANGE = SlashBlade.prefix("sange");
    public static final ResourceLocation FOX_BLACK = SlashBlade.prefix("fox_black");
    public static final ResourceLocation FOX_WHITE = SlashBlade.prefix("fox_white");

    public static final ResourceLocation RODAI_WOODEN = SlashBlade.prefix("rodai_wooden");
    public static final ResourceLocation RODAI_STONE = SlashBlade.prefix("rodai_stone");
    public static final ResourceLocation RODAI_IRON = SlashBlade.prefix("rodai_iron");
    public static final ResourceLocation RODAI_GOLDEN = SlashBlade.prefix("rodai_golden");
    public static final ResourceLocation RODAI_DIAMOND = SlashBlade.prefix("rodai_diamond");
    public static final ResourceLocation RODAI_NETHERITE = SlashBlade.prefix("rodai_netherite");

    public static final ResourceLocation TAGAYASAN = SlashBlade.prefix("tagayasan");
    public static final ResourceLocation AGITO = SlashBlade.prefix("agito");
    public static final ResourceLocation AGITO_RUST = SlashBlade.prefix("agito_rust");
    public static final ResourceLocation OROTIAGITO = SlashBlade.prefix("orotiagito");
    public static final ResourceLocation OROTIAGITO_SEALED = SlashBlade.prefix("orotiagito_sealed");
    public static final ResourceLocation OROTIAGITO_RUST = SlashBlade.prefix("orotiagito_rust");

    public static final ResourceLocation YASHA = SlashBlade.prefix("yasha");
    public static final ResourceLocation YASHA_TRUE = SlashBlade.prefix("yasha_true");

    public static final ResourceLocation SABIGATANA = SlashBlade.prefix("sabigatana");
    public static final ResourceLocation SABIGATANA_BROKEN = SlashBlade.prefix("sabigatana_broken");
    public static final ResourceLocation DOUTANUKI = SlashBlade.prefix("doutanuki");
    public static final ResourceLocation KOSEKI = SlashBlade.prefix("koseki");

    public static void registerAll() {
        // 1.19.2鐗堟湰涓紝鎴戜滑闇€瑕佹墜鍔ㄦ敞鍐屾墍鏈夋嫈鍒€鍓戝畾涔?
        // 杩欓噷浣跨敤涓庡師鐗坢od鐩稿悓鐨勬敞鍐岄€昏緫
        registerBladeDefinition(KOSEKI,
                new SlashBladeDefinition(SlashBlade.prefix("koseki"),
                        RenderDefinition.Builder
                                .newInstance()
                                .effectColor(0x303030)
                                .textureName(SlashBlade.prefix("model/named/dios/koseki.png"))
                                .modelName(SlashBlade.prefix("model/named/dios/dios.obj"))
                                .standbyRenderType(CarryType.NINJA)
                                .build(),
                        PropertiesDefinition.Builder.newInstance()
                                .baseAttackModifier(5.0F).maxDamage(70)
                                .defaultSwordType(List.of(SwordType.BEWITCHED))
                                .slashArtsType(SlashArtsRegistry.DRIVE_VERTICAL.getId())
                                .addSpecialEffect(SpecialEffectsRegistry.WITHER_EDGE.getId())
                                .build(),
                        List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.POWER_ARROWS), 2))));

        registerBladeDefinition(SABIGATANA,
                new SlashBladeDefinition(SlashBlade.prefix("sabigatana"),
                        RenderDefinition.Builder
                                .newInstance().textureName(SlashBlade.prefix("model/named/muramasa/sabigatana.png"))
                                .modelName(SlashBlade.prefix("model/named/muramasa/muramasa.obj"))
                                .standbyRenderType(CarryType.PSO2)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(3.0F).maxDamage(40).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(SABIGATANA_BROKEN, new SlashBladeDefinition(SlashBlade.prefix("sabigatana"),
                RenderDefinition.Builder
                        .newInstance().textureName(SlashBlade.prefix("model/named/muramasa/sabigatana.png"))
                        .modelName(SlashBlade.prefix("model/named/muramasa/muramasa.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(3.0F).maxDamage(40)
                        .defaultSwordType(List.of(SwordType.BROKEN, SwordType.SEALED)).build(),
                Lists.newArrayList()));

        registerBladeDefinition(DOUTANUKI,
                new SlashBladeDefinition(SlashBlade.prefix("doutanuki"),
                        RenderDefinition.Builder.newInstance()
                                .textureName(SlashBlade.prefix("model/named/muramasa/doutanuki.png"))
                                .modelName(SlashBlade.prefix("model/named/muramasa/muramasa.obj"))
                                .standbyRenderType(CarryType.PSO2)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F).maxDamage(60)
                                .slashArtsType(SlashArtsRegistry.CIRCLE_SLASH.getId())
                                .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(TAGAYASAN,
                new SlashBladeDefinition(SlashBlade.prefix("tagayasan"),
                        RenderDefinition.Builder.newInstance()
                                .textureName(SlashBlade.prefix("model/named/tagayasan.png"))
                                .standbyRenderType(CarryType.KATANA)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F).maxDamage(70)
                                .slashArtsType(SlashArtsRegistry.DRIVE_VERTICAL.getId())
                                .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                        List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.SMITE), 3),
                                new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 3))));

        registerBladeDefinition(YASHA, new SlashBladeDefinition(SlashBlade.prefix("yasha"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/yasha/yasha.png"))
                        .modelName(SlashBlade.prefix("model/named/yasha/yasha.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(6.0F)
                        .slashArtsType(SlashArtsRegistry.SAKURA_END.getId()).maxDamage(70).build(),
                Lists.newArrayList()));

        registerBladeDefinition(YASHA_TRUE, new SlashBladeDefinition(SlashBlade.prefix("yasha_true"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/yasha/yasha.png"))
                        .modelName(SlashBlade.prefix("model/named/yasha/yasha_true.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(6.0F)
                        .slashArtsType(SlashArtsRegistry.CIRCLE_SLASH.getId())
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).maxDamage(70).build(),
                Lists.newArrayList()));

        registerBladeDefinition(AGITO_RUST, new SlashBladeDefinition(SlashBlade.prefix("agito_rust"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/agito_rust.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(3.0F).maxDamage(60)
                        .defaultSwordType(List.of(SwordType.SEALED)).build(),
                Lists.newArrayList()));

        registerBladeDefinition(AGITO, new SlashBladeDefinition(SlashBlade.prefix("agito"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/agito_false.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F)
                        .slashArtsType(SlashArtsRegistry.WAVE_EDGE.getId()).maxDamage(60).build(),
                Lists.newArrayList()));

        registerBladeDefinition(OROTIAGITO_RUST, new SlashBladeDefinition(SlashBlade.prefix("orotiagito_rust"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/agito_rust_true.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(3.0F).maxDamage(60)
                        .defaultSwordType(List.of(SwordType.SEALED)).build(),
                Lists.newArrayList()));

        registerBladeDefinition(OROTIAGITO_SEALED, new SlashBladeDefinition(SlashBlade.prefix("orotiagito_sealed"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/agito_true.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F)
                        .slashArtsType(SlashArtsRegistry.WAVE_EDGE.getId()).maxDamage(60).build(),
                Lists.newArrayList()));

        registerBladeDefinition(OROTIAGITO, new SlashBladeDefinition(SlashBlade.prefix("orotiagito"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/orotiagito.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F)
                        .slashArtsType(SlashArtsRegistry.WAVE_EDGE.getId())
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).maxDamage(60).build(),
                Lists.newArrayList()));

        registerBladeDefinition(RODAI_WOODEN,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_wooden"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/rodai_wooden.png"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(2.0F).maxDamage(60).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RODAI_STONE,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_stone"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/rodai_stone.png"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(3.0F).maxDamage(132).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RODAI_IRON,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_iron"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/rodai_iron.png"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(4.0F).maxDamage(250).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RODAI_GOLDEN,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_golden"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/rodai_golden.png"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(2.0F).maxDamage(33).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RODAI_DIAMOND,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_diamond"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/rodai_diamond.png"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F).maxDamage(1561).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RODAI_NETHERITE,
                new SlashBladeDefinition(SlashBlade.prefix("rodai_netherite"),
                        RenderDefinition.Builder.newInstance()
                                .textureName(SlashBlade.prefix("model/rodai_netherite.png"))
                                .standbyRenderType(CarryType.DEFAULT).build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(8.0F).maxDamage(2031).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(RUBY,
                new SlashBladeDefinition(SlashBlade.prefix("ruby"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/ruby.png"))
                                .standbyRenderType(CarryType.DEFAULT).build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F).maxDamage(45).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(FOX_BLACK, new SlashBladeDefinition(SlashBlade.prefix("fox_black"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/sange/black.png"))
                        .modelName(SlashBlade.prefix("model/named/sange/sange.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F).maxDamage(70)
                        .slashArtsType(SlashArtsRegistry.PIERCING.getId())
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.SMITE), 4),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.KNOCKBACK), 2),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 2))));

        registerBladeDefinition(FOX_WHITE, new SlashBladeDefinition(SlashBlade.prefix("fox_white"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/sange/white.png"))
                        .modelName(SlashBlade.prefix("model/named/sange/sange.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(5.0F).maxDamage(70)
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.KNOCKBACK), 2),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.BANE_OF_ARTHROPODS), 2),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 3),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 2),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.MOB_LOOTING), 3))));

        registerBladeDefinition(YAMATO,
                new SlashBladeDefinition(SlashBlade.prefix("yamato"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/yamato.png"))
                                .modelName(SlashBlade.prefix("model/named/yamato.obj"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F)
                                .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                        List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.SOUL_SPEED), 2),
                                new EnchantmentDefinition(getEnchantmentID(Enchantments.POWER_ARROWS), 5),
                                new EnchantmentDefinition(getEnchantmentID(Enchantments.FALL_PROTECTION), 4))));

        registerBladeDefinition(YAMATO_BROKEN,
                new SlashBladeDefinition(SlashBlade.prefix("yamato"),
                        RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/yamato.png"))
                                .modelName(SlashBlade.prefix("model/named/yamato.obj"))
                                .standbyRenderType(CarryType.DEFAULT)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F)
                                .defaultSwordType(List.of(SwordType.BROKEN, SwordType.SEALED)).build(),
                        List.of()));

        registerBladeDefinition(TUKUMO, new SlashBladeDefinition(SlashBlade.prefix("yuzukitukumo"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/a_tukumo.png"))
                        .modelName(SlashBlade.prefix("model/named/agito.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(6.0F)
                        .slashArtsType(SlashArtsRegistry.DRIVE_HORIZONTAL.getId())
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                List.of(new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 1),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.SHARPNESS), 4),
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 3))));

        registerBladeDefinition(MURAMASA,
                new SlashBladeDefinition(SlashBlade.prefix("muramasa"),
                        RenderDefinition.Builder
                                .newInstance().textureName(SlashBlade.prefix("model/named/muramasa/muramasa.png"))
                                .modelName(SlashBlade.prefix("model/named/muramasa/muramasa.obj"))
                                .standbyRenderType(CarryType.PSO2)
                                .build(),
                        PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F).maxDamage(50)
                                .slashArtsType(SlashArtsRegistry.DRIVE_VERTICAL.getId()).build(),
                        Lists.newArrayList()));

        registerBladeDefinition(SANGE, new SlashBladeDefinition(SlashBlade.prefix("sange"),
                RenderDefinition.Builder.newInstance().textureName(SlashBlade.prefix("model/named/sange/sange.png"))
                        .modelName(SlashBlade.prefix("model/named/sange/sange.obj"))
                        .standbyRenderType(CarryType.PSO2)
                        .build(),
                PropertiesDefinition.Builder.newInstance().baseAttackModifier(6.0F).maxDamage(70)
                        .slashArtsType(SlashArtsRegistry.VOID_SLASH.getId())
                        .defaultSwordType(List.of(SwordType.BEWITCHED)).build(),
                Lists.newArrayList()));
    }

    private static void registerBladeDefinition(ResourceLocation key, SlashBladeDefinition definition) {
        SlashBladeRegistryHandler.cacheBladeDefinition(
                net.minecraft.resources.ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, key),
                definition
        );
        MinecraftForge.EVENT_BUS.post(new SlashBladeRegistryEvent.Register(key, definition));
    }

    private static ResourceLocation getEnchantmentID(Enchantment enchantment) {
        return ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
    }

    
}
