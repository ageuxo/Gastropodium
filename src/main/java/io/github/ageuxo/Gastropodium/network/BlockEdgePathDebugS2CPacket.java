package io.github.ageuxo.Gastropodium.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgePath;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlockEdgePathDebugS2CPacket extends PathDebugS2CPacket<BlockEdgePath> {
    public static Codec<BlockEdgePathDebugS2CPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockEdgePath.CODEC.fieldOf("path").forGetter(BlockEdgePathDebugS2CPacket::getPath),
            Codec.INT.fieldOf("id").forGetter(BlockEdgePathDebugS2CPacket::getMobId),
            Codec.FLOAT.fieldOf("maxDistanceToWaypoint").forGetter(BlockEdgePathDebugS2CPacket::getMaxDistanceToWaypoint)
    ).apply(instance, BlockEdgePathDebugS2CPacket::new));

    public BlockEdgePathDebugS2CPacket(BlockEdgePath path, int mobId, float maxDistanceToWaypoint) {
        super(path, mobId, maxDistanceToWaypoint);
    }

    public static void handle(BlockEdgePathDebugS2CPacket msg, Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->ClientPacketHandler.handleBlockEdgePathDebugPacket(msg)));
        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, this);
    }

    public static BlockEdgePathDebugS2CPacket decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

}
