package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.world.level.pathfinder.PathFinder;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockEdgePathFinder extends PathFinder {

    public BlockEdgePathFinder(BlockEdgeNodeEvaluator pNodeEvaluator, int pMaxVisitedNodes) {
        super(pNodeEvaluator, pMaxVisitedNodes);
    }

/*    @Override
    public @NotNull Path reconstructPath(Node pPoint, BlockPos pTargetPos, boolean pReachesTarget) {
        BlockEdgeNode blockEdgeNode = (BlockEdgeNode) pPoint;
        List<BlockEdgeNode> list = Lists.newArrayList();
        BlockEdgeNode node = blockEdgeNode;
        list.add(0, blockEdgeNode);

        while (node.cameFrom != null){
            node = (BlockEdgeNode) node.cameFrom;
            list.add(0, node);
        }

        return new BlockEdgePath(list, pTargetPos, pReachesTarget);
    }*/

}
