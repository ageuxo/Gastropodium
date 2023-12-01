package io.github.ageuxo.Gastropodium.entity.pathing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.ageuxo.Gastropodium.mixins.PathfindingRendererAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.Map;

@ParametersAreNonnullByDefault
public class BlockEdgePathfindingRenderer extends PathfindingRenderer {
    protected static float MAX_RENDER_DIST = 80.0F;

    private final PathfindingRendererAccessor accessor = ((PathfindingRendererAccessor)this);// is this a bad idea?

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
        Map<Integer, Path> pathMap = accessor.getPathMap();
        if (!pathMap.isEmpty()) {
            long time = Util.getMillis();

            for(Integer key : pathMap.keySet()) {
                BlockEdgePath path = (BlockEdgePath) pathMap.get(key);
                this.renderPath(pPoseStack, pBuffer, path, pCamX, pCamY, pCamZ);
            }

            Map<Integer, Long> creationMap = accessor.getCreationMap();
            for(Integer key : creationMap.keySet().toArray(new Integer[0])) {
                if (time - creationMap.get(key) > 5000L) {
                    pathMap.remove(key);
                    creationMap.remove(key);
                }
            }
        }
    }

    public void renderPath(PoseStack poseStack, MultiBufferSource bufferSource, BlockEdgePath path, double camX, double camY, double camZ){
        renderPathLine(poseStack, bufferSource.getBuffer(RenderType.debugLineStrip(6)), path, camX, camY, camZ);
        BlockPos blockPos = path.getTarget();
        if (PathfindingRendererAccessor.callDistanceToCamera(blockPos, camX, camY, camZ) <= MAX_RENDER_DIST){
            DebugRenderer.renderFilledBox(poseStack, bufferSource, new AABB(blockPos.getX() + 0.25, blockPos.getY() + 0.25, blockPos.getZ() + 0.25, blockPos.getX() + 0.75, blockPos.getY() + 0.75, blockPos.getZ() + 0.75).move(-camX, -camY, -camZ), 0.0F, 1.0F, 0.0F, 0.5F);

            for (int i = 0; i < path.getNodeCount(); ++i){
                BlockEdgeNode node = path.getEdgeNode(i);
                if (PathfindingRendererAccessor.callDistanceToCamera(blockPos, camX, camY, camZ) <= MAX_RENDER_DIST){
                    float red = i == path.getNextNodeIndex() ? 1.0F : 0.0F;
                    float blue = i == path.getNextNodeIndex() ? 0.0F : 1.0F;
                    DebugRenderer.renderFilledBox(poseStack, bufferSource, makeBlockEdgePosAABB(node).move(-camX, -camY, -camZ), red, 0.0F, blue, 0.5F);
                }
            }
        }

        for (int j = 0; j < path.getNodeCount(); ++j){
            BlockEdgeNode node = path.getEdgeNode(j);
            if (PathfindingRendererAccessor.callDistanceToCamera(blockPos, camX, camY, camZ) <= MAX_RENDER_DIST){
                Vec3 typeVec = getOffsetVec3(node, 0.2D);
                DebugRenderer.renderFloatingText(poseStack, bufferSource, String.valueOf(node.type), typeVec.x, typeVec.y, typeVec.z, -1, 0.015F, true, 0.0F, true);
                Vec3 malusVec = getOffsetVec3(node, 0.4D);
                DebugRenderer.renderFloatingText(poseStack, bufferSource, String.format(Locale.ROOT, "%.2f", node.costMalus), malusVec.x, malusVec.y, malusVec.z, -1, 0.015F, true, 0.0F, true);
            }
        }
    }

    public static void renderPathLine(PoseStack poseStack, VertexConsumer consumer, BlockEdgePath path, double camX, double camY, double camZ){
        for (int i = 0; i < path.getNodeCount(); ++i){
            BlockEdgeNode node = path.getEdgeNode(i);
            if (!(PathfindingRendererAccessor.callDistanceToCamera(node.asBlockPos(), camX, camY, camZ) > MAX_RENDER_DIST)){
                float hue = (float) i / path.getNodeCount() * 0.33F;
                int j = i == 0 ? 0 : Mth.hsvToRgb(hue, 0.9F, 0.9F);
                int red = j >> 16 & 255;
                int green = j >> 8 & 255;
                int blue = j & 255;
                Vec3 point = getOffsetVec3(node, 0.05);
                consumer.vertex(poseStack.last().pose(), (float) ((float) point.x - camX), (float) ((float) point.y - camY), (float) ((float) point.z - camZ)).color(red, blue, green, 255).endVertex();
            }
        }
    }

    protected AABB makeBlockEdgePosAABB(BlockEdgeNode node){
        AABB aabb = new AABB(node.asBlockPos());
        aabb = aabb.deflate(0.1D);
        var offset = node.edge.getOpposite().getNormal();
        return aabb.contract((double) offset.getX() * 0.5D, (double) offset.getY() * 0.5D, (double) offset.getZ() * 0.5D);
    }

    protected static Vec3 getOffsetVec3(BlockEdgeNode node, double offset){
        Vec3i normal = node.edge.getNormal();
        Vec3 center = node.asBlockPos().getCenter();
        return center.add((double) normal.getX() * offset, (double) normal.getY() * offset, (double) normal.getZ() * offset);
    }
}
