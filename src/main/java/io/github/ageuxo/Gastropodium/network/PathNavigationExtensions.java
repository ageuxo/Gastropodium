package io.github.ageuxo.Gastropodium.network;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public interface PathNavigationExtensions<T extends Path> {
    void sendPathfindingPacket(Level level, Mob mob, T path, float maxDistanceToTarget);
}
