package io.github.ageuxo.Gastropodium.entity.pathing;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockEdgeNodeEvaluator extends NodeEvaluator {
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;

    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectArrayMap<>();

    @Override
    public void prepare(PathNavigationRegion level, Mob mob) {
        this.level = level;
        this.mob = mob;
        this.nodes.clear();
        this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0F);
    }

    @Override
    public @NotNull Node getStart() {
        Node startNode = this.getNode(this.mob.blockPosition());
        startNode.type = this.getBlockPathType(this.mob, startNode.asBlockPos());
        startNode.costMalus = this.mob.getPathfindingMalus(startNode.type);

        return startNode;
    }

    @Override
    public @NotNull Target getGoal(double pX, double pY, double pZ) {
        return this.getTargetFromNode(this.getNode(Mth.floor(pX), Mth.floor(pY), Mth.floor(pZ)));
    }

/*    @Override
    public int getNeighbors(Node[] pOutputArray, Node pNode) {
        int neighborCount = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        Direction[] directions = Direction.orderedByNearest(this.mob);
        // Iterate over 3x3 area around node
        for (int relativeX = -1; relativeX < 1; relativeX++) {
            mutableBlockPos.set(pNode.asBlockPos());
            mutableBlockPos.move(directions[0], relativeX);
            for (int relativeY = -1; relativeY < 1; relativeY++) {
                mutableBlockPos.move(directions[1], relativeY);

                BlockEdgeNode neighbor = this.getNode(mutableBlockPos);
                if (isNeighborValid(neighbor, pNode)){
                    pOutputArray[neighborCount++] = neighbor;
                }
            }
        }
        return neighborCount;
    }*/

    @Override
    public int getNeighbors(Node[] outputArray, Node node){
        BlockEdgeNode edgeNode = (BlockEdgeNode) node;
        int neighborCount = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        List<Direction> directions = new ArrayList<>();
        Collections.addAll(directions, Direction.values());
        directions.remove(edgeNode.edge);
        directions.remove(edgeNode.edge.getOpposite());
        for (Direction direction : directions){
            mutableBlockPos.set(node.asBlockPos());
            mutableBlockPos.move(direction);
            BlockEdgeNode neighbor;
            if (isPathfindable(mutableBlockPos)){
                neighbor = this.getEdgeNode(mutableBlockPos, edgeNode.edge);
                if (isNeighborValid(neighbor, node)){
                    outputArray[neighborCount++] = neighbor;
                } else {
                    mutableBlockPos.move(edgeNode.edge);
                    if (isPathfindable(mutableBlockPos)){
                        neighbor = this.getEdgeNode(mutableBlockPos, direction.getOpposite());
                        if (isNeighborValid(neighbor, node)){
                            outputArray[neighborCount++] = neighbor;
                        }
                    }
                }
            } else {
                neighbor = this.getEdgeNode(node.asBlockPos(), direction);
                if (isNeighborValid(neighbor, node)){
                    outputArray[neighborCount++] = neighbor;
                }
            }
        }

        return neighborCount;
    }

    private boolean isPathfindable(BlockPos.MutableBlockPos mutableBlockPos) {
        return this.level.getBlockState(mutableBlockPos).isPathfindable(level, mutableBlockPos, PathComputationType.LAND);
    }

/*    @Override
    protected @NotNull BlockEdgeNode getNode(BlockPos pPos) {
        ArrayList<Direction> edges = new ArrayList<>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()){
            mutableBlockPos.set(pPos);
            mutableBlockPos.move(direction);
            if (!this.level.getBlockState(mutableBlockPos).isPathfindable(this.level, mutableBlockPos, PathComputationType.LAND)){
                edges.add(direction);
            }
        }
        return new BlockEdgeNode(pPos, edges);
        //Remember that only edges are walkable
    }*/

    protected @Nullable BlockEdgeNode getEdgeNode(BlockPos pos, Direction edge){
        BlockPos checkPos = pos.relative(edge);
        if (!this.level.getBlockState(checkPos).isPathfindable(this.level, checkPos, PathComputationType.LAND)){
            return new BlockEdgeNode(pos, edge);
        } else {
            return null;
        }
    }

    protected boolean isNeighborValid(@Nullable Node pNeighbor, Node pNode) {
        return pNeighbor != null && !pNeighbor.closed && (pNeighbor.costMalus >= 0.0F || pNode.costMalus < 0.0F);
    }

    @Override
    public @NotNull BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ, Mob pMob) {
        return this.getBlockPathType(pLevel, pX, pY, pZ);
    }

    @Override
    public @NotNull BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ) {
        return WalkNodeEvaluator.getBlockPathTypeStatic(pLevel, new BlockPos.MutableBlockPos(pX, pY, pZ));
    }

    protected BlockPathTypes getCachedBlockType(Mob pEntity, int pX, int pY, int pZ) {
        long longPos = BlockPos.asLong(pX, pY, pZ);
        return this.pathTypesByPosCache.computeIfAbsent(longPos, (d) -> this.getBlockPathType(this.level, pX, pY, pZ, pEntity));
    }

    protected BlockPathTypes getBlockPathType(Mob pEntityliving, BlockPos pPos) {
        return this.getCachedBlockType(pEntityliving, pPos.getX(), pPos.getY(), pPos.getZ());
    }
}
