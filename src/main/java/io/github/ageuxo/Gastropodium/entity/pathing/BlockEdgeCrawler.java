package io.github.ageuxo.Gastropodium.entity.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public interface BlockEdgeCrawler {
    boolean isAttached();
    Direction getAttachDirection();
    void setAttachDirection(Direction direction);

/*    static boolean canAttachInPos(Mob mob, BlockPos pos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()){
            mutableBlockPos.set(pos.relative(direction));
            BlockState state = mob.level().getBlockState(pos);
            if (state.isSolid() && state.getBlockPathType(mob.level(), mutableBlockPos, mob) != null
                    && state.getBlockPathType(mob.level(), mutableBlockPos, mob).getMalus() >= 0F){
                return true;
            }
        }
        return false;
    }*/

    default boolean attach(Mob mob, BlockPos pos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()){
            mutableBlockPos.set(pos.relative(direction));
            BlockState state = mob.level().getBlockState(pos);
            if (state.isSolid() && isBlockSafe(mob.level(), mutableBlockPos, state, mob)){
                this.setAttachDirection(direction);
                return true;
            }
        }
        return false;
    }

    default boolean isBlockSafe(BlockGetter blockGetter, BlockPos pos, BlockState state, Mob mob){
        BlockPathTypes pathType = state.getBlockPathType(blockGetter, pos, mob);
        return pathType == null || pathType.getDanger() == null;
    }

}
