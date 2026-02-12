package mods.flammpfeil.slashblade.client;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.LockonCircleRender;
import mods.flammpfeil.slashblade.client.renderer.gui.RankRenderer;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.event.client.SneakingMotionCanceller;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.util.LoaderUtil;

import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(BladeModelManager.getInstance());
        MinecraftForge.EVENT_BUS.register(BladeMotionManager.getInstance());

        SneakingMotionCanceller.getInstance().register();

        UserPoseOverrider.getInstance().register();
        LockonCircleRender.getInstance().register();

        RankRenderer.getInstance().register();

        ItemProperties.register(SlashBladeItems.SLASHBLADE.get(), new ResourceLocation("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_BAMBOO.get(), new ResourceLocation("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), new ResourceLocation("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_WHITE.get(), new ResourceLocation("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_WOOD.get(), new ResourceLocation("slashblade:user"),
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SlashBladeKeyMappings.KEY_SPECIAL_MOVE);
        event.register(SlashBladeKeyMappings.KEY_SUMMON_BLADE);
    }

    // �?.19.2中，使用ModelEvent.BakingCompleted事件
    @SubscribeEvent
    public static void Baked(final ModelEvent.BakingCompleted event) {
        bakeBlade(SlashBladeItems.SLASHBLADE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WHITE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WOOD.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_BAMBOO.get(), event);
    }

    public static void bakeBlade(Item blade, final ModelEvent.BakingCompleted event) {
        ModelResourceLocation loc = new ModelResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(blade)), "inventory");
        BladeModel model = new BladeModel(event.getModels().get(loc), event.getModelBakery());
        event.getModels().put(loc, model);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        addPlayerLayer(event, "default");
        addPlayerLayer(event, "slim");

        Minecraft mc = Minecraft.getInstance();
        // 畜生forge给的什么破事件还带强制转换�? 必须得我这样写，好不优雅
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
            addEntityLayer(event, mc.getEntityRenderDispatcher().renderers.get(type));
        }

//        addEntityLayer(event, EntityType.ZOMBIE);
//        addEntityLayer(event, EntityType.HUSK);
//        addEntityLayer(event, EntityType.ZOMBIE_VILLAGER);
//
//        addEntityLayer(event, EntityType.WITHER_SKELETON);
//        addEntityLayer(event, EntityType.SKELETON);
//        addEntityLayer(event, EntityType.STRAY);
//
//        addEntityLayer(event, EntityType.PIGLIN);
//        addEntityLayer(event, EntityType.PIGLIN_BRUTE);
//        addEntityLayer(event, EntityType.ZOMBIFIED_PIGLIN);
    }

    @SuppressWarnings({"unchecked"})
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers evt, String skin) {
        EntityRenderer<? extends Player> renderer = evt.getSkin(skin);

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }

    @SuppressWarnings({"unchecked"})
    private static void addEntityLayer(EntityRenderersEvent.AddLayers evt, EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }


}






