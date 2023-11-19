package io.github.ageuxo.Gastropodium.entity.pathing;

import com.mojang.logging.LogUtils;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockEdgeNodeEvaluator extends NodeEvaluator {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<BlockEdgeNode> nodes = new Int2ObjectOpenHashMap<>();

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
    public @NotNull BlockEdgeNode getStart() {
        BlockEdgeNode startNode = this.getEdgeNode(this.mob.blockPosition(), ((BlockEdgeCrawler)this.mob).getAttachDirection());
        startNode.type = this.getBlockPathType(this.mob, startNode.asBlockPos());
        startNode.costMalus = this.mob.getPathfindingMalus(startNode.type);
        return startNode;
    }

    @Override
    public @NotNull BlockEdgeTarget getGoal(double pX, double pY, double pZ) {
        return this.nodes.values().stream().filter(node -> node.asBlockPos().equals(BlockPos.containing(pX, pY, pZ))).findFirst().map(BlockEdgeTarget::new).orElseThrow();
    }

    public BlockEdgeTarget getGoal(BlockEdgeNode edgeNode){
        return new BlockEdgeTarget(edgeNode);
    }

    @Override
    public int getNeighbors(Node[] outputArray, Node node){
        int neighborCount = 0;
        BlockEdgeNode edgeNode = (BlockEdgeNode) node;
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

    private boolean isPathfindable(BlockPos blockPos) {
        return this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND);
    }

    protected BlockEdgeNode getEdgeNode(BlockPos pos, Direction edge){
        BlockPos checkPos = pos.relative(edge);
        if (!this.level.getBlockState(checkPos).isPathfindable(this.level, checkPos, PathComputationType.LAND)){
            BlockEdgeNode node = new BlockEdgeNode(pos, edge);
            node.type = getBlockPathType(this.mob, pos);
            return node;
        } else {
            BlockEdgeNode node = new BlockEdgeNode(pos, Direction.DOWN);
            node.type = getBlockPathType(this.mob, pos);
            return node;
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
        return this.pathTypesByPosCache.computeIfAbsent(longPos, (d) -> this.getBlockPathTypeRaw(this.level, pX, pY, pZ, pEntity));
    }

    protected BlockPathTypes getBlockPathType(Mob pEntityliving, BlockPos pPos) {
        return this.getCachedBlockType(pEntityliving, pPos.getX(), pPos.getY(), pPos.getZ());
    }

    public BlockPathTypes getBlockPathTypeRaw(BlockGetter level, int x, int y, int z, Mob mob) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        for (Direction direction : Direction.values()){
            BlockPos relative = pos.relative(direction);
            BlockState state = level.getBlockState(relative);
            BlockPathTypes type = state.getBlockPathType(level, relative, mob);
            if (type != null && type.getDanger() != null) {
                return type.getDanger();
            }
        }

        return this.isPathfindable(pos.set(x, y, z)) ? BlockPathTypes.WALKABLE : BlockPathTypes.BLOCKED;
    }
}
