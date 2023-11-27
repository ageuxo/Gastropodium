package io.github.ageuxo.Gastropodium.entity.pathing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ageuxo.Gastropodium.mixins.PathAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@MethodsReturnNonnullByDefault
public class BlockEdgePath extends Path {
    public static Codec<BlockEdgePath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockEdgeNode.CODEC.listOf().fieldOf("nodes").forGetter(BlockEdgePath::getEdgeNodes),
            BlockEdgeNode.CODEC.fieldOf("target").forGetter(BlockEdgePath::getTargetNode),
            Codec.BOOL.fieldOf("reached").forGetter(BlockEdgePath::canReach),
            Codec.INT.fieldOf("nextNode").forGetter(BlockEdgePath::getNextNodeIndex)
    ).apply(instance, BlockEdgePath::new));
    private final BlockEdgeNode target;

    public BlockEdgePath(List<BlockEdgeNode> edgeNodes, BlockEdgeNode target, boolean reached, int nextIndex) {
        super(edgeNodes.stream().map(edgeNode -> (Node)edgeNode).toList(), target.asBlockPos(), reached);
        this.target = target;
    }

    public BlockEdgePath(List<BlockEdgeNode> list, BlockEdgeNode pTargetPos, boolean pReachesTarget) {
        this(list, pTargetPos, pReachesTarget, 0);
    }

    @Override
    public Vec3 getEntityPosAtNode(@NotNull Entity pEntity, int pIndex) {
        BlockEdgeNode node = this.getEdgeNode(pIndex);
        Vec3 entityPos = node.asBlockPos().getCenter();
        Vec3 offset = Vec3.ZERO.relative(node.edge, 0.5);
        return entityPos.add(offset);
    }

    public BlockEdgeNode getEdgeNode(int index){
        return (BlockEdgeNode) ((PathAccessor)this).getNodes().get(index);
    }

    public List<BlockEdgeNode> getEdgeNodes(){
        return ((PathAccessor)this).getNodes().stream().map(node -> (BlockEdgeNode)node).toList();
    }

    @Override
    public BlockPos getTarget() {
        return this.target.asBlockPos();
    }

    public BlockEdgeNode getTargetNode(){
        return this.target;
    }

    @Override
    public boolean sameAs(@Nullable Path pPathentity) {
        BlockEdgePath newPath = (BlockEdgePath) pPathentity;
        if (pPathentity == null || newPath.getNodeCount() != this.getNodeCount()){
            return false;
        } else {
            for (int i = 0; i < this.getNodeCount(); ++i){
                BlockEdgeNode node = this.getEdgeNode(i);
                BlockEdgeNode newNode = newPath.getEdgeNode(i);
                if (node.x != newNode.x || node.y != newNode.y || node.z != newNode.z || node.edge != newNode.edge){
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return "BlockEdgePath(length="+this.getNodeCount()+")";
    }
}
