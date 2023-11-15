package io.github.ageuxo.Gastropodium.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.ageuxo.Gastropodium.GastropodiumMod;
import io.github.ageuxo.Gastropodium.entity.BasicSlugEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BasicSlugRenderer extends MobRenderer<BasicSlugEntity, BasicSlugModel<BasicSlugEntity>> {
    public BasicSlugRenderer(EntityRendererProvider.Context pContext){
        super(pContext, new BasicSlugModel<>(pContext.bakeLayer(ModelLayers.SLUG_LAYER)), 0.2f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BasicSlugEntity pEntity) {
        return GastropodiumMod.modRL("textures/entity/basic_slug.png");
    }

    @Override
    public void render(BasicSlugEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()){
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    protected void setupRotations(@NotNull BasicSlugEntity slugEntity, @NotNull PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(slugEntity, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(slugEntity.getXRot()));
//        pPoseStack.mulPose(Axis.ZP.rotationDegrees(slugEntity.visZRot)); TODO implement this when pathing works
    }
}
