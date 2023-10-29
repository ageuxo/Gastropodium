package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockEdgePath extends Path {
    public BlockEdgePath(List<Node> pNodes, BlockPos pTarget, boolean pReached) {
        super(pNodes, pTarget, pReached);
    }

    @Override
    public @NotNull Vec3 getEntityPosAtNode(@NotNull Entity pEntity, int pIndex) {
        BlockEdgeNode node = (BlockEdgeNode) this.getNode(pIndex);
        Vec3 entityPos = node.asBlockPos().getCenter();
        Vec3 offset = Vec3.ZERO.relative(node.edge, 0.5);
        return entityPos.add(offset);
    }
}
