package io.github.ageuxo.Gastropodium.network;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.pathfinder.Path;
import org.slf4j.Logger;

public class ClientPacketHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static void handleBlockEdgePathDebugPacket(BlockEdgePathDebugS2CPacket packet){
        Minecraft instance = Minecraft.getInstance();
        if (instance.level != null){
            instance.debugRenderer.pathfindingRenderer
                    .addPath(packet.getMobId(), packet.getPath(), packet.getMaxDistanceToWaypoint());
        }
    }

    public static void handleVanillaPathDebugPacket(VanillaPathDebugS2CPacket packet) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.level != null){
            Path path = packet.getPath();
//            VanillaDebugPacketHelper.logDebugInfo(packet.getMobId(), path);
            instance.debugRenderer.pathfindingRenderer
                    .addPath(packet.getMobId(), path, packet.getMaxDistanceToWaypoint());
        }
    }

}
