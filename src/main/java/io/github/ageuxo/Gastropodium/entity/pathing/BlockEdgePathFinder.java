package io.github.ageuxo.Gastropodium.entity.pathing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class BlockEdgePathFinder extends PathFinder {
    private final int maxVisitedNodes;
    private final BlockEdgeNodeEvaluator nodeEvaluator;
    private final BlockEdgeNode[] neighbors = new BlockEdgeNode[32];
    private final BlockEdgeBinaryHeap openSet = new BlockEdgeBinaryHeap();


    public BlockEdgePathFinder(BlockEdgeNodeEvaluator nodeEvaluator, int maxVisitedNodes) {
        super(nodeEvaluator, maxVisitedNodes);
        this.maxVisitedNodes = maxVisitedNodes;
        this.nodeEvaluator = nodeEvaluator;
    }

    @Nullable
    @Override
    public Path findPath(PathNavigationRegion pRegion, Mob pMob, Set<BlockPos> pTargetPositions, float pMaxRange, int pAccuracy, float pSearchDepthMultiplier) {
        Set<BlockEdgeNode> targetNodes = new HashSet<>();
        pTargetPositions.forEach(blockPos -> {
            BlockPos.MutableBlockPos pos = blockPos.mutable();
            for (Direction direction : Direction.values()){
                BlockPos.MutableBlockPos moved = pos.move(direction);
                if (pRegion.getBlockState(moved).entityCanStandOnFace(pRegion, moved, pMob, direction.getOpposite())){
                    targetNodes.add(this.nodeEvaluator.getEdgeNode(blockPos, direction));
                }
            }
        });
        return findEdgePath(pRegion, pMob, targetNodes, pMaxRange, pAccuracy, pSearchDepthMultiplier);
    }

    @Nullable
    public BlockEdgePath findEdgePath(PathNavigationRegion region, Mob mob, Set<BlockEdgeNode> targetNodes, float maxRange, int accuracy, float searchDepthMultiplier) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(region, mob);
        BlockEdgeNode node = this.nodeEvaluator.getStart();
        Map<BlockEdgeTarget, BlockEdgeNode> map = targetNodes.stream().collect(Collectors.toMap(this.nodeEvaluator::getGoal, Function.identity()));

        BlockEdgePath path = this.findPath(region.getProfiler(), node, map, maxRange, accuracy, searchDepthMultiplier);
        this.nodeEvaluator.done();
        return path;
    }

    public float distance(BlockEdgeNode first, BlockEdgeNode second) {
        return super.distance(first, second);
    }

    private BlockEdgePath findPath(ProfilerFiller pProfiler, BlockEdgeNode pNode, Map<BlockEdgeTarget, BlockEdgeNode> targetMap, float pMaxRange, int pAccuracy, float pSearchDepthMultiplier) {
        pProfiler.push("find_path");
        pProfiler.markForCharting(MetricCategory.PATH_FINDING);
        Set<BlockEdgeTarget> set = targetMap.keySet();
        pNode.g = 0.0F;
        pNode.h = this.getBestH(pNode, set);
        pNode.f = pNode.h;
        this.openSet.clear();
        this.openSet.insert(pNode);
        int i = 0;
        Set<BlockEdgeTarget> set2 = Sets.newHashSetWithExpectedSize(set.size());
        int j = (int)((float)this.maxVisitedNodes * pSearchDepthMultiplier);

        while(!this.openSet.isEmpty()) {
            ++i;
            if (i >= j) {
                break;
            }

            BlockEdgeNode node = this.openSet.pop();
            node.closed = true;

            for(BlockEdgeTarget target : set) {
                if (node.distanceManhattan(target) <= (float)pAccuracy) {
                    target.setReached();
                    set2.add(target);
                }
            }

            if (!set2.isEmpty()) {
                break;
            }

            if (!(node.distanceTo(pNode) >= pMaxRange)) {
                int k = this.nodeEvaluator.getNeighbors(this.neighbors, node);

                for(int l = 0; l < k; ++l) {
                    BlockEdgeNode node1 = this.neighbors[l];
                    float f = this.distance(node, node1);
                    node1.walkedDistance = node.walkedDistance + f;
                    float f1 = node.g + f + node1.costMalus;
                    if (node1.walkedDistance < pMaxRange && (!node1.inOpenSet() || f1 < node1.g)) {
                        node1.cameFrom = node;
                        node1.g = f1;
                        node1.h = this.getBestH(node1, set) * 1.5F;
                        if (node1.inOpenSet()) {
                            this.openSet.changeCost(node1, node1.g + node1.h);
                        } else {
                            node1.f = node1.g + node1.h;
                            this.openSet.insert(node1);
                        }
                    }
                }
            }
        }

        Optional<BlockEdgePath> optional = !set2.isEmpty() ?
                set2.stream().map( (target) -> this.reconstructPath(target.getBestNode(), targetMap.get(target), true))
                        .min(Comparator.comparingInt(BlockEdgePath::getNodeCount)) :
                set.stream().map( (target) -> this.reconstructPath(target.getBestNode(), targetMap.get(target), false))
                        .min(Comparator.comparingDouble(BlockEdgePath::getDistToTarget).thenComparingInt(BlockEdgePath::getNodeCount));
        pProfiler.pop();
        return optional.orElse(null);
    }

    private float getBestH(BlockEdgeNode node, Set<BlockEdgeTarget> targets) {
        float bestH = Float.MAX_VALUE;

        for (BlockEdgeTarget target : targets){
            float targetH = node.distanceTo(target);
            target.updateBest(targetH, node);
            bestH = Math.min(targetH, bestH);
        }
        return bestH;
    }

    private BlockEdgePath reconstructPath(BlockEdgeNode pPoint, BlockEdgeNode pTargetPos, boolean pReachesTarget) {
        List<BlockEdgeNode> list = Lists.newArrayList();
        BlockEdgeNode node = pPoint;
        list.add(0, pPoint);

        while(node.cameFrom != null) {
            node = node.cameFrom;
            list.add(0, node);
        }

        return new BlockEdgePath(list, pTargetPos, pReachesTarget);
    }
}
