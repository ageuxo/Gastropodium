package io.github.ageuxo.Gastropodium.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PathNavigation.class)
public interface PathNavigationAccessor {

    @Accessor
    void setTargetPos(BlockPos pos);

    @Accessor
    void setReachRange(int range);

    @Accessor
    float getMaxVisitedNodesMultiplier();

    @Invoker
    void callResetStuckTimeout();
}
