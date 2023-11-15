package io.github.ageuxo.Gastropodium.entity.pathing;

import com.google.common.collect.ImmutableSet;
import io.github.ageuxo.Gastropodium.mixins.PathNavigationAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class BlockEdgePathNavigation extends PathNavigation {
    protected BlockEdgeNodeEvaluator edgeNodeEvaluator;
    private final BlockEdgeCrawler crawler;
    protected final BlockEdgePathFinder edgePathFinder;

    public BlockEdgePathNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
        if (pMob instanceof BlockEdgeCrawler) {
            this.crawler = (BlockEdgeCrawler) pMob;
        } else {
            throw new IllegalArgumentException("BlockEdgePathNavigation must be passed Mob that implements BlockEdgeCrawler");
        }
        int i = Mth.floor(pMob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
        this.edgePathFinder = this.createPathFinder(i);
    }

    @Nullable
    @Override
    public BlockEdgePath getPath() {
        return (BlockEdgePath) this.path;
    }

    @Nullable
    protected BlockEdgePath createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy, float pFollowRange) {
        if (pTargets.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && pTargets.contains(this.getTargetPos())) {
            return (BlockEdgePath) this.path;
        } else {
            this.level.getProfiler().push("pathfind");
            BlockPos blockpos = pOffsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int i = (int)(pFollowRange + (float)pRegionOffset);
            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
            BlockEdgePath path = this.edgePathFinder.findEdgePath(pathnavigationregion, this.mob, findNodesAtTargets(pTargets, this.level, this.mob), pFollowRange, pAccuracy, ((PathNavigationAccessor)this).getMaxVisitedNodesMultiplier());
            this.level.getProfiler().pop();
            if (path != null) {
                ((PathNavigationAccessor) this).setTargetPos(path.getTarget());
                ((PathNavigationAccessor) this).setReachRange(pAccuracy);
                ((PathNavigationAccessor) this).callResetStuckTimeout();
            }

            return path;
        }
    }

    @Override
    public boolean moveTo(@NotNull Entity entity, double speed) {
        BlockEdgePath path = this.createPath(ImmutableSet.of(entity.blockPosition()), 16, false, 1, (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
        return path != null && this.moveTo(path, speed);
    }

    protected static Set<BlockEdgeNode> findNodesAtTargets(Set<BlockPos> targets, BlockGetter blockGetter, Mob mob){
        Set<BlockEdgeNode> nodeSet = new HashSet<>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos targetPos : targets){
            for (Direction direction : Direction.values()){
                mutableBlockPos.set(targetPos);
                mutableBlockPos.move(direction);
                if (canWalkOn(blockGetter, mob, direction, mutableBlockPos)) {
                    nodeSet.add(new BlockEdgeNode(mutableBlockPos, direction));
                }/* else if (canWalkOn(blockGetter, mob, direction, mutableBlockPos.below())) {
                    nodeSet.add(new BlockEdgeNode(mutableBlockPos.below(), direction));
                }*/ //TODO cornerEdges, maybe via CornerEdgeNode?
            }
        }
        return nodeSet;
    }

    private static boolean canWalkOn(BlockGetter blockGetter, Mob mob, Direction direction, BlockPos mutableBlockPos) {
        return !blockGetter.getBlockState(mutableBlockPos).isPathfindable(blockGetter, mutableBlockPos, PathComputationType.LAND)
                && blockGetter.getBlockState(mutableBlockPos).entityCanStandOnFace(blockGetter, mutableBlockPos, mob, direction.getOpposite());
    }

    @Override
    protected @NotNull BlockEdgePathFinder createPathFinder(int pMaxVisitedNodes) {
        this.edgeNodeEvaluator = new BlockEdgeNodeEvaluator();
        this.edgeNodeEvaluator.setCanPassDoors(true);
        return new BlockEdgePathFinder(this.edgeNodeEvaluator, pMaxVisitedNodes);
    }

    @Override
    @NotNull
    public Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(), this.mob.getZ());
    }

    @Override
    public boolean canUpdatePath() {
        return crawler.isAttached() || crawler.attach(mob, mob.blockPosition());
    }

    @Override
    public @NotNull BlockEdgeNodeEvaluator getNodeEvaluator() {
        return this.edgeNodeEvaluator;
    }

    @Override
    public void setCanFloat(boolean pCanSwim) {
        this.edgeNodeEvaluator.setCanFloat(pCanSwim);
    }

    @Override
    public boolean canFloat() {
        return this.edgeNodeEvaluator.canFloat();
    }

    @Override
    protected double getGroundY(Vec3 pVec) {
        return pVec.y;
    }

    @Override
    protected void trimPath() {}
}
