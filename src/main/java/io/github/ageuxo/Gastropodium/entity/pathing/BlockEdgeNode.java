package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.pathfinder.Node;

public class BlockEdgeNode extends Node {
    public final Direction edge;

    public BlockEdgeNode(int pX, int pY, int pZ, Direction edge) {
        super(pX, pY, pZ);
        this.edge = edge;
    }

    public BlockEdgeNode(BlockPos pos, Direction edge){
        this(pos.getX(), pos.getY(), pos.getZ(), edge);
    }
}
