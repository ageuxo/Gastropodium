package io.github.ageuxo.Gastropodium.network;

import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void handleBlockEdgePathDebugPacket(BlockEdgePathDebugS2CPacket packet){
        Minecraft instance = Minecraft.getInstance();
        if (instance.level != null){
            instance.debugRenderer.pathfindingRenderer
                    .addPath(packet.getMobId(), packet.getPath(), packet.getMaxDistanceToWaypoint());
        }
    }
}
