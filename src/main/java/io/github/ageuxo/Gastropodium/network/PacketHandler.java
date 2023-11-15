package io.github.ageuxo.Gastropodium.network;

import io.github.ageuxo.Gastropodium.GastropodiumMod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            GastropodiumMod.modRL("main"),
            ()->PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(){
        int id = 0;
        INSTANCE.registerMessage(id++, BlockEdgePathDebugS2CPacket.class, BlockEdgePathDebugS2CPacket::encode, BlockEdgePathDebugS2CPacket::decode, BlockEdgePathDebugS2CPacket::handle);
    }
}
