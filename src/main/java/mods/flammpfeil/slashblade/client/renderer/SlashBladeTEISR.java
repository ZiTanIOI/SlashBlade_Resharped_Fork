package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;

public class SlashBladeTEISR extends BlockEntityWithoutLevelRenderer {

    public SlashBladeTEISR(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(@NotNull ItemStack itemStackIn, ItemTransforms.TransformType type, @NotNull PoseStack matrixStack, 
                             @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!(itemStackIn.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        renderBlade(itemStackIn, type, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    boolean checkRenderNaked() {
        ItemStack mainHand = BladeModel.user.getMainHandItem();
        return !(mainHand.getItem() instanceof ItemSlashBlade);
    }

    public boolean renderBlade(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack, 
                               MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        if (transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
                || transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemTransforms.TransformType.NONE) {

            if (BladeModel.user == null) {
                final Minecraft minecraftInstance = Minecraft.getInstance();
                BladeModel.user = minecraftInstance.player;
            }

            boolean handle = false;

            if (BladeModel.user != null) {
                handle = BladeModel.user.getMainArm() == HumanoidArm.RIGHT
                        ? transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                        : transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            }

            if (handle) {
                BladeFirstPersonRender.getInstance().render(matrixStack, bufferIn, combinedLightIn);
            }

            return false;
        }

        try (MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {

            matrixStack.translate(0.5f, 0.5f, 0.5f);

            if (transformType == ItemTransforms.TransformType.GROUND) {
                matrixStack.translate(0, 0.15f, 0);
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn, 0.005f);
            } else if (transformType == ItemTransforms.TransformType.GUI) {
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn, 0.008f, true);
            } else if (transformType == ItemTransforms.TransformType.FIXED) {
                if (stack.isFramed() && stack.getFrame() instanceof BladeStandEntity) {
                    renderModel(stack, matrixStack, bufferIn, combinedLightIn);
                } else {
                    matrixStack.mulPose(new Quaternion(0, 180.0f, 0, true));
                    renderIcon(stack, matrixStack, bufferIn, combinedLightIn, 0.0095f);
                }
            } else {
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn, 0.0095f);
            }
        }

        return true;
    }

    public void renderIcon(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, 
                           float scale) {
        renderIcon(stack, matrixStack, bufferIn, lightIn, scale, false);
    }

    public void renderIcon(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, 
                           float scale, boolean renderDurability) {

        matrixStack.scale(scale, scale, scale);

        EnumSet<SwordType> types = SwordType.from(stack);

        ResourceLocation modelLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s -> s.getModel().isPresent()).map(s -> s.getModel().orElseGet(() -> stackDefaultModel(stack)))
                .orElseGet(() -> stackDefaultModel(stack));
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
        ResourceLocation textureLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s -> s.getTexture().isPresent()).map(s -> s.getTexture().orElseGet(() -> stackDefaultTexture(stack)))
                .orElseGet(() -> stackDefaultTexture(stack));

        String renderTarget;
        if (types.contains(SwordType.BROKEN)) {
            renderTarget = "item_damaged";
        } else if (types.contains(SwordType.NOSCABBARD)) {
            renderTarget = "item_bladens";
        } else {
            renderTarget = "item_blade";
        }

        BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, lightIn);
        BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, matrixStack, 
                bufferIn, lightIn);

        if (renderDurability) {

            WavefrontObject durabilityModel = BladeModelManager.getInstance()
                    .getModel(DefaultResources.resourceDurabilityModel);

            float durability = (float) stack.getDamageValue() / (float) stack.getMaxDamage();
            matrixStack.translate(0.0F, 0.0F, 0.1f);

            Color aCol = new Color(0.25f, 0.25f, 0.25f, 1.0f);
            Color bCol = new Color(0xA52C63);
            int r = 0xFF & (int) Mth.lerp(aCol.getRed(), bCol.getRed(), durability);
            int g = 0xFF & (int) Mth.lerp(aCol.getGreen(), bCol.getGreen(), durability);
            int b = 0xFF & (int) Mth.lerp(aCol.getBlue(), bCol.getBlue(), durability);

            BladeRenderState.setCol(new Color(r, g, b));
            BladeRenderState.renderOverrided(stack, durabilityModel, "base", DefaultResources.resourceDurabilityTexture, 
                    matrixStack, bufferIn, lightIn);

            boolean isBroken = types.contains(SwordType.BROKEN);
            matrixStack.translate(0.0F, 0.0F, -2.0f * durability);

            BladeRenderState.renderOverrided(stack, durabilityModel, isBroken ? "color_r" : "color", 
                    DefaultResources.resourceDurabilityTexture, matrixStack, bufferIn, lightIn);

        }
    }

    public ResourceLocation stackDefaultModel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return DefaultResources.resourceDefaultModel;
        }
        CompoundTag stateTag = stack.getTagElement("bladeState");
        String name = null;
        if (stateTag != null) {
            name = stateTag.getString("ModelName");
        }
        if (!(stack.getItem() instanceof ItemSlashBladeDetune)) {
            ResourceLocation bladeName = getBladeNameFromState(stateTag);
            if (bladeName != null) {
                Registry<SlashBladeDefinition> registry = BladeModelManager.getClientSlashBladeRegistry();
                if (registry != null) {
                    SlashBladeDefinition slashBladeDefinition = registry.get(bladeName);
                    if (slashBladeDefinition != null) {
                        name = slashBladeDefinition.getRenderDefinition().getModelName().toString();
                    }
                }
            }
        }
        if (name != null) {
            return !name.isBlank()
                    ? ResourceLocation.tryParse(name) : DefaultResources.resourceDefaultModel;
        }
        return DefaultResources.resourceDefaultModel;
    }

    public ResourceLocation stackDefaultTexture(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("bladeState")) {
            return DefaultResources.resourceDefaultTexture;
        }
        CompoundTag stateTag = stack.getTagElement("bladeState");
        String name = null;
        if (stateTag != null) {
            name = stateTag.getString("TextureName");
        }
        if (!(stack.getItem() instanceof ItemSlashBladeDetune)) {
            ResourceLocation bladeName = getBladeNameFromState(stateTag);
            if (bladeName != null) {
                Registry<SlashBladeDefinition> registry = BladeModelManager.getClientSlashBladeRegistry();
                if (registry != null) {
                    SlashBladeDefinition slashBladeDefinition = registry.get(bladeName);
                    if (slashBladeDefinition != null) {
                        name = slashBladeDefinition.getRenderDefinition().getTextureName().toString();
                    }
                }
            }
        }
        if (name != null) {
            return !name.isBlank()
                    ? ResourceLocation.tryParse(name) : DefaultResources.resourceDefaultTexture;
        }
        return DefaultResources.resourceDefaultTexture;
    }

    private ResourceLocation getBladeNameFromState(CompoundTag stateTag) {
        if (stateTag == null) {
            return null;
        }
        String key = stateTag.getString("translationKey");
        if (key == null || key.isBlank() || !key.startsWith("item.")) {
            return null;
        }
        return ResourceLocation.tryParse(key.substring(5).replaceFirst("\\.", ":"));
    }

    public void renderModel(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn) {

        float scale = 0.003125f;
        matrixStack.scale(scale, scale, scale);
        float defaultOffset = 130;
        matrixStack.translate(defaultOffset, 0, 0);

        EnumSet<SwordType> types = SwordType.from(stack);

        ResourceLocation modelLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s -> s.getModel().isPresent()).map(s -> s.getModel().orElseGet(() -> stackDefaultModel(stack)))
                .orElseGet(() -> stackDefaultModel(stack));
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
        ResourceLocation textureLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s -> s.getTexture().isPresent()).map(s -> s.getTexture().orElseGet(() -> stackDefaultTexture(stack)))
                .orElseGet(() -> stackDefaultTexture(stack));

        Vec3 bladeOffset = Vec3.ZERO;
        float bladeOffsetRot = 0;
        float bladeOffsetBaseRot = -3;
        Vec3 sheathOffset = Vec3.ZERO;
        float sheathOffsetRot = 0;
        float sheathOffsetBaseRot = -3;
        boolean vFlip = false;
        boolean hFlip = false;
        boolean hasScabbard = true;

        if (stack.isFramed()) {
            if (stack.getFrame() instanceof BladeStandEntity stand) {
                Item type = stand.currentType;

                Pose pose = stand.getPose();
                switch (pose.ordinal()) {
                    case 0:
                        break;
                    case 1:
                        vFlip = true;
                        break;
                    case 2:
                        vFlip = true;
                        hFlip = true;
                        break;
                    case 3:
                        hFlip = true;
                        break;
                    case 4:
                        hasScabbard = false;
                        break;
                    case 5:
                        hFlip = true;
                        hasScabbard = false;
                        break;
                }

                if (type == SlashBladeItems.BLADESTAND_2.get()) {
                    bladeOffset = new Vec3(0, 21.5f, 0);
                    if (hFlip) {
                        sheathOffset = new Vec3(-40, -27, 0);
                    } else {
                        sheathOffset = new Vec3(40, -27, 0);
                    }
                    sheathOffsetBaseRot = -4;
                } else if (type == SlashBladeItems.BLADESTAND_V.get()) {
                    bladeOffset = new Vec3(-100, 230, 0);
                    sheathOffset = new Vec3(-100, 230, 0);
                    bladeOffsetRot = 80;
                    sheathOffsetRot = 80;
                } else if (type == SlashBladeItems.BLADESTAND_S.get()) {
                    if (hFlip) {
                        bladeOffset = new Vec3(60, -25, 0);
                        sheathOffset = new Vec3(60, -25, 0);
                    } else {
                        bladeOffset = new Vec3(-60, -25, 0);
                        sheathOffset = new Vec3(-60, -25, 0);
                    }
                } else if (type == SlashBladeItems.BLADESTAND_1_W.get()) {
                } else if (type == SlashBladeItems.BLADESTAND_2_W.get()) {
                    bladeOffset = new Vec3(0, 21.5f, 0);
                    if (hFlip) {
                        sheathOffset = new Vec3(-40, -27, 0);
                    } else {
                        sheathOffset = new Vec3(40, -27, 0);
                    }
                    sheathOffsetBaseRot = -4;
                }
            }
        }

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
            String renderTarget;
            if (types.contains(SwordType.BROKEN)) {
                renderTarget = "blade_damaged";
            } else {
                renderTarget = "blade";
            }

            matrixStack.translate(bladeOffset.x, bladeOffset.y, bladeOffset.z);
            matrixStack.mulPose(new Quaternion(0, 0, bladeOffsetRot, true));

            if (vFlip) {
                matrixStack.mulPose(new Quaternion(180, 0, 0, true));
                matrixStack.translate(0, -15, 0);

                matrixStack.translate(0, 5, 0);
            }

            if (hFlip) {
                double offset = defaultOffset;
                matrixStack.translate(-offset, 0, 0);
                matrixStack.mulPose(new Quaternion(0, 180, 0, true));
                matrixStack.translate(offset, 0, 0);
            }

            matrixStack.mulPose(new Quaternion(0, 0, bladeOffsetBaseRot, true));

            BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, 
                    lightIn);
            BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, 
                    matrixStack, bufferIn, lightIn);
        }

        if (hasScabbard) {
            try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                String renderTarget = "sheath";

                matrixStack.translate(sheathOffset.x, sheathOffset.y, sheathOffset.z);
            matrixStack.mulPose(new Quaternion(0, 0, sheathOffsetRot, true));

            if (vFlip) {
                matrixStack.mulPose(new Quaternion(180, 0, 0, true));
                matrixStack.translate(0, -15, 0);

                matrixStack.translate(0, 5, 0);
            }

            if (hFlip) {
                double offset = defaultOffset;
                matrixStack.translate(-offset, 0, 0);
                matrixStack.mulPose(new Quaternion(0, 180, 0, true));
                matrixStack.translate(offset, 0, 0);
            }

            matrixStack.mulPose(new Quaternion(0, 0, sheathOffsetBaseRot, true));

                BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, 
                        lightIn);
                BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, 
                        matrixStack, bufferIn, lightIn);
            }
        }

    }
}
