package io.github.ageuxo.Gastropodium.mixins;

import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(PathfindingRenderer.class)
public interface PathfindingRendererAccessor {
    @Invoker
    static float callDistanceToCamera(BlockPos pPos, double pX, double pY, double pZ) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    Map<Integer, Path> getPathMap();

    @Accessor
    Map<Integer, Float> getPathMaxDist();

    @Accessor
    Map<Integer, Long> getCreationMap();
}
