package io.github.ageuxo.Gastropodium.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgePathfindingRenderer;
import io.github.ageuxo.Gastropodium.entity.pathing.DebugRendererExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixins implements DebugRendererExtension {
    @Shadow @Final public PathfindingRenderer pathfindingRenderer;
    @Unique
    public BlockEdgePathfindingRenderer gastropodium$blockEdgePathfindingRenderer = new BlockEdgePathfindingRenderer();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V", at = @At(value = "RETURN"))
    public void onRender(PoseStack pPoseStack, MultiBufferSource.BufferSource pBufferSource, double pCamX, double pCamY, double pCamZ, CallbackInfo ci){
        this.pathfindingRenderer.render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
        this.gastropodium$blockEdgePathfindingRenderer.render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
    }

    @Unique
    @Override
    public BlockEdgePathfindingRenderer gastropodium$getBlockEdgePathfindingRenderer() {
        return gastropodium$blockEdgePathfindingRenderer;
    }
}
