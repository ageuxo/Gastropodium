package io.github.ageuxo.Gastropodium.mixins;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixins {
    @Redirect(method = {"tick", "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", "followThePath", }, at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;path:Lnet/minecraft/world/level/pathfinder/Path;", opcode = 180))
    private Path getPathInTick(PathNavigation pathNavigation) {
        return pathNavigation.getPath();
    }
}
