package io.github.ageuxo.Gastropodium.entity.pathing;

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
public class BlockEdgePath extends Path{
    private final BlockEdgeNode[] closedSet = new BlockEdgeNode[0];
    private final BlockEdgeNode[] openSet = new BlockEdgeNode[0];
    private final BlockEdgeNode target;
    private final boolean reached;
    private final List<BlockEdgeNode> edgeNodes;
    private final float distToTarget;
    private int nextIndex = 0;

    public BlockEdgePath(List<BlockEdgeNode> edgeNodes, BlockEdgeNode target, boolean reached) {
        super(edgeNodes.stream().map(edgeNode -> (Node)edgeNode).toList(), target.asBlockPos(), reached);
        this.edgeNodes = edgeNodes;
        this.target = target;
        this.distToTarget = edgeNodes.isEmpty() ? Float.MAX_VALUE : getEdgeNode(edgeNodes.size()-1).distanceTo(target);
        this.reached = reached;
    }

    @Override
    public void advance() {
        ++this.nextIndex;
    }

    @Override
    public boolean notStarted() {
        return this.nextIndex <= 0;
    }

    @Override
    public boolean isDone() {
        return this.nextIndex >= this.edgeNodes.size();
    }

    @Override
    public int getNextNodeIndex() {
        return this.nextIndex;
    }

    @Override
    public void setNextNodeIndex(int index) {
        this.nextIndex = index;
    }

    @Override
    public Vec3 getEntityPosAtNode(@NotNull Entity pEntity, int pIndex) {
        BlockEdgeNode node = this.getEdgeNode(pIndex);
        Vec3 entityPos = node.asBlockPos().getCenter();
        Vec3 offset = Vec3.ZERO.relative(node.edge, 0.5);
        return entityPos.add(offset);
    }

    @Override
    public boolean canReach() {
        return this.reached;
    }

    public BlockEdgeNode getEdgeNode(int index){
        return this.edgeNodes.get(index);
    }

    @Override
    public BlockEdgeNode[] getOpenSet() {
        return openSet;
    }

    @Override
    public BlockEdgeNode[] getClosedSet() {
        return closedSet;
    }

    @Override
    public BlockPos getTarget() {
        return this.target.asBlockPos();
    }

    @Override
    public float getDistToTarget() {
        return this.distToTarget;
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
    public int getNodeCount() {
        return this.edgeNodes.size();
    }

    @Override
    public String toString() {
        return "BlockEdgePath(length="+this.edgeNodes.size()+")";
    }
}
