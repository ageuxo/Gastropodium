package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockEdgePathNavigation extends GroundPathNavigation {
    private final BlockEdgeCrawler crawler;

    public BlockEdgePathNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
        if (!(pMob instanceof BlockEdgeCrawler)){
            throw new  IllegalArgumentException("BlockEdgePathNavigation must be passed Mob that implements BlockEdgeCrawler");
        }
        this.crawler = (BlockEdgeCrawler) this.mob;
    }

    @Override
    public BlockEdgePath createPath(@NotNull BlockPos pPos, int pAccuracy) {

        return (BlockEdgePath) super.createPath(pPos, pAccuracy);
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int pMaxVisitedNodes) {
        this.nodeEvaluator = new BlockEdgeNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
    }

    @Override
    protected @NotNull Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(), this.mob.getZ());
    }

    @Override
    protected boolean canUpdatePath() {
        return crawler.isAttached();
    }
}
