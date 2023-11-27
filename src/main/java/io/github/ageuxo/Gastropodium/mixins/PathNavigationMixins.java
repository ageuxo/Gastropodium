package io.github.ageuxo.Gastropodium.mixins;

import io.github.ageuxo.Gastropodium.GastropodiumMod;
import io.github.ageuxo.Gastropodium.entity.pathing.PathNavigationExtensions;
import io.github.ageuxo.Gastropodium.network.VanillaDebugPacketHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PathNavigation.class)
@Implements(value = @Interface(iface = PathNavigationExtensions.class, prefix = "pathNavExt$"))
public abstract class PathNavigationMixins{
    @Redirect(method = {"tick", "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", "followThePath", }, at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;path:Lnet/minecraft/world/level/pathfinder/Path;", opcode = 180))
    private Path getPathInTick(PathNavigation pathNavigation) {
        return pathNavigation.getPath();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/DebugPackets;sendPathFindingPacket(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/pathfinder/Path;F)V"))
    public void sendNavigationPathfindingPacket(Level pLevel, Mob pMob, Path pPath, float pMaxDistanceToWaypoint){
        if (GastropodiumMod.DEBUG){
            this.pathNavExt$sendPathfindingPacket(pLevel, pMob, pPath, pMaxDistanceToWaypoint);
        }
    }

    public <T extends Path> void pathNavExt$sendPathfindingPacket(Level level, Mob mob, T path, float maxDistanceToTarget) {
        VanillaDebugPacketHelper.sendVanillaPathfindingPacket(level, mob, path, maxDistanceToTarget);
    }
}
