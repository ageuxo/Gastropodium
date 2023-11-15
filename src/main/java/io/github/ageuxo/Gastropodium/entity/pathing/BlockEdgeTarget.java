package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.core.Direction;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.NotNull;

public class BlockEdgeTarget extends Target {
    public final Direction edge;
    private float bestHeuristic = Float.MAX_VALUE;
    private BlockEdgeNode bestNode;
    private boolean reached;

    public BlockEdgeTarget(BlockEdgeNode node){
        super(node.x, node.y, node.z);
        this.edge = node.edge;
    }

    public void updateBest(float heuristic, BlockEdgeNode node){
        if (heuristic < this.bestHeuristic){
            this.bestHeuristic = heuristic;
            this.bestNode = node;
        }
    }

    public @NotNull BlockEdgeNode getBestNode() {
        return bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }
}
