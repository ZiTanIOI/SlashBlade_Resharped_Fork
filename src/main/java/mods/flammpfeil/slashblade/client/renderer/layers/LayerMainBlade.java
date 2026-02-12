package mods.flammpfeil.slashblade.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.init.DefaultResources;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LayerMainBlade<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public LayerMainBlade(RenderLayerParent<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    public float modifiedSpeed(float baseSpeed, LivingEntity entity) {
        float modif = 6.0f;
        if (MobEffectUtil.hasDigSpeed(entity)) {
            modif = 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            modif = 6 + (1 + Objects.requireNonNull(entity.getEffect(MobEffects.DIG_SLOWDOWN)).getAmplifier()) * 2;
        }

        modif /= 6.0f;

        return baseSpeed / modif;
    }

    public void renderOffhandItem(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity) {

        ItemStack offhandStack = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandStack.isEmpty() || !offhandStack.getCapability(CapabilitySlashBlade.BLADESTATE).isPresent()) {
            renderHotbarItem(matrixStack, bufferIn, lightIn, entity);
            return;
        }

        renderStandbyBlade(matrixStack, bufferIn, lightIn, offhandStack, entity);
    }

    public void renderHotbarItem(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity) {
        if (entity instanceof Player player) {
            if (player.getInventory().selected == 0) {
                return;
            }

            ItemStack blade = player.getInventory().getItem(0);
            if (blade.isEmpty()) {
                return;
            }

            renderStandbyBlade(matrixStack, bufferIn, lightIn, blade, entity);
        }
    }

    public void renderStandbyBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack blade, T entity) {
        LazyOptional<ISlashBladeState> state = blade.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> {
            double modelScaleBase = 0.0078125F; // 0.5^7
            double motionScale = 1.5 / 12.0;
            ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);

            WavefrontObject obj = BladeModelManager.getInstance()
                    .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));
            String part;
            try (MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {
                // minecraft model neckPoint height = 1.5f
                // mmd model neckPoint height = 12.0f
                matrixStack.translate(0, 1.5f, 0);
                var carrytype = s.getCarryType();
                final Minecraft mcinstance = Minecraft.getInstance();
                switch (carrytype) {
                    case PSO2:
                        matrixStack.translate(1F, -1.125f, 0.20f);
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-7)); // 近似于-0.122173弧度
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    case KATANA:
                        matrixStack.translate(0.25F, -0.875f, -0.55f);
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(15));
                        break;

                    case DEFAULT:
                        matrixStack.translate(0.25F, -0.875f, -0.55f);
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(15));
                        break;

                    case NINJA:
                        matrixStack.translate(-0.5F, -2f, 0.20f);
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-120));
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    case RNINJA:
                        matrixStack.translate(0.5F, -2f, 0.20f);
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-60));
                        if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON
                                && entity == mcinstance.player) {
                            return;
                        }
                        break;

                    default:
                        return;
                }

                float modelScale = (float) (modelScaleBase * (1.0f / motionScale));
                matrixStack.scale((float) motionScale, (float) motionScale, (float) motionScale);
                matrixStack.scale(modelScale, modelScale, modelScale);

                try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                    if (s.isBroken()) {
                        part = "blade_damaged";
                    } else {
                        part = "blade";
                    }

                    BladeRenderState.renderOverrided(blade, obj, part, textureLocation, matrixStack, bufferIn,
                            lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj, part + "_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);
                    BladeRenderState.renderOverrided(blade, obj, "sheath", textureLocation, matrixStack, bufferIn,
                            lightIn);
                    BladeRenderState.renderOverridedLuminous(blade, obj, "sheath_luminous", textureLocation,
                            matrixStack, bufferIn, lightIn);
                }
            }
        });
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int lightIn, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 渲染副手物品
        this.renderOffhandItem(matrixStack, bufferIn, lightIn, entity);

        // 渲染主手拔刀剑
        ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.isEmpty()) {
            return;
        }

        LazyOptional<ISlashBladeState> state = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> {
            // 跳过第一人称主手渲染（由BladeFirstPersonRender处理）
            final Minecraft mcinstance = Minecraft.getInstance();
            if (mcinstance.options.getCameraType() == CameraType.FIRST_PERSON && entity == mcinstance.player) {
                return;
            }

            String part;
            if (s.isBroken()) {
                part = "blade_damaged";
            } else {
                part = "blade";
            }

            ResourceLocation textureLocation = s.getTexture().orElse(DefaultResources.resourceDefaultTexture);
            WavefrontObject obj = BladeModelManager.getInstance()
                    .getModel(s.getModel().orElse(DefaultResources.resourceDefaultModel));

            // 正确使用玩家模型的手骨位置渲染刀
            try (MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {
                // 1.19.2版本兼容：直接设置手部位置偏移
                // 计算手部位置，考虑玩家姿势
                double handX = entity.getMainHandItem() == stack ? 0.08 : 0.0;
                double handY = 0.45;
                double handZ = -0.15;
                
                // 设置刀的位置和旋转
                matrixStack.translate(handX, handY, handZ);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
                
                float modelScale = 0.015625F; // 1/64 缩放，修复模型过小问题
                matrixStack.scale(modelScale, modelScale, modelScale);
                
                // 渲染刀身
                BladeRenderState.renderOverrided(stack, obj, part, textureLocation, matrixStack, bufferIn, lightIn);
                BladeRenderState.renderOverridedLuminous(stack, obj, part + "_luminous", textureLocation, matrixStack, bufferIn, lightIn);
                // 渲染刀鞘
                BladeRenderState.renderOverrided(stack, obj, "sheath", textureLocation, matrixStack, bufferIn, lightIn);
                BladeRenderState.renderOverridedLuminous(stack, obj, "sheath_luminous", textureLocation, matrixStack, bufferIn, lightIn);
            }
        });
    }
}