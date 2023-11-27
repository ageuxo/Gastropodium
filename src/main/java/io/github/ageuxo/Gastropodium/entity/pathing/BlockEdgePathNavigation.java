package io.github.ageuxo.Gastropodium.entity.pathing;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import io.github.ageuxo.Gastropodium.mixins.PathNavigationAccessor;
import io.github.ageuxo.Gastropodium.network.PathNavigationExtensions;
import io.github.ageuxo.Gastropodium.network.VanillaDebugPacketHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class BlockEdgePathNavigation extends PathNavigation implements PathNavigationExtensions<BlockEdgePath> {
    public static final Logger LOGGER = LogUtils.getLogger();
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
        if (pTargets.isEmpty() || this.mob.getY() < (double) this.level.getMinBuildHeight() || !this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && pTargets.contains(this.getTargetPos())) {
            return getPath();
        } else {
            this.level.getProfiler().push("pathfind");
            BlockPos blockpos = pOffsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int i = (int) (pFollowRange + (float) pRegionOffset);
            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
            BlockEdgePath path = this.edgePathFinder.findEdgePath(pathnavigationregion, this.mob, findNodesAtTargets(pTargets, this.level, this.mob), pFollowRange, pAccuracy, ((PathNavigationAccessor) this).getMaxVisitedNodesMultiplier());
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

    protected void followThePath() {
        Vec3 mobPos = this.getTempMobPos();
        MoveControl moveControl = this.mob.getMoveControl();
//        LOGGER.debug("{}, {} {} {}", mobPos, moveControl.getWantedX(), moveControl.getWantedY(), moveControl.getWantedZ());
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        if (isNextNodeInRange() && this.shouldTargetNextNodeInDirection(mobPos)) {
            this.getPath().advance();
        }

        this.doStuckDetection(mobPos);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 mobPos) {
        if (this.getPath().getNextNodeIndex() + 1 >= this.getPath().getNodeCount()) {
            return false;
        } else {
            Vec3 nodePos = this.getPath().getNextEntityPos(this.mob);
            if (!mobPos.closerThan(nodePos, this.mob.getBbWidth() >= 1.0D ? this.mob.getBbWidth() : 1.0D)) {
                return false;
            } else {
                Vec3 nextNodePos = this.getPath().getEntityPosAtNode(this.mob, this.getPath().getNextNodeIndex() + 1);
                Vec3 deltaNodePos = nodePos.subtract(mobPos); // TODO I don't really understand this
                Vec3 deltaNextNodePos = nextNodePos.subtract(mobPos);
                double nodePosLength = deltaNodePos.lengthSqr();
                double nextNodePosLength = deltaNextNodePos.lengthSqr();
                boolean nextNodeCloserThanNode = nextNodePosLength < nodePosLength;
                boolean currentlyAtNodePos = nodePosLength < 0.5D;
                if (!nextNodeCloserThanNode && !currentlyAtNodePos) {
                    return false;
                } else {
                    Vec3 nodePosNormal = deltaNodePos.normalize();
                    Vec3 nextNodePosNormal = deltaNextNodePos.normalize();
                    return nextNodePosNormal.dot(nodePosNormal) < 0.0D;
                }
            }
        }
    }

    private boolean isNextNodeInRange() {
        Vec3i nodePos = this.getPath().getNextNodePos();
        double dX = Math.abs(this.mob.getX() - ((double)nodePos.getX() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        double dY = Math.abs(this.mob.getY() - ((double)nodePos.getY() + (this.mob.getBbHeight()) / 2D));
        double dZ = Math.abs(this.mob.getZ() - ((double)nodePos.getZ() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        return dX <= (double) this.maxDistanceToWaypoint && dZ <= (double) this.maxDistanceToWaypoint && dY <= (double) this.maxDistanceToWaypoint;
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
        return crawler.isAttached();
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

    @Override
    public void sendPathfindingPacket(Level level, Mob mob, BlockEdgePath path, float maxDistanceToTarget) {
        VanillaDebugPacketHelper.sendBlockEdgePathfindingPacket(level, mob, path, maxDistanceToTarget);
    }
}
