package io.github.ageuxo.Gastropodium.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VanillaPathDebugS2CPacket extends PathDebugS2CPacket<Path> {
    public static Codec<VanillaPathDebugS2CPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VanillaDebugPacketHelper.PATH_CODEC.fieldOf("path").forGetter(VanillaPathDebugS2CPacket::getPath),
            Codec.INT.fieldOf("id").forGetter(VanillaPathDebugS2CPacket::getMobId),
            Codec.FLOAT.fieldOf("maxDistanceToWaypoint").forGetter(VanillaPathDebugS2CPacket::getMaxDistanceToWaypoint)
    ).apply(instance, VanillaPathDebugS2CPacket::new));

    public VanillaPathDebugS2CPacket(Path path, int mobId, float maxDistanceToWaypoint) {
        super(path, mobId, maxDistanceToWaypoint);
    }

    public static void handle(VanillaPathDebugS2CPacket type, Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->ClientPacketHandler.handleVanillaPathDebugPacket(type)));
        ctx.setPacketHandled(true);
    }

    @Override
    public void encode(FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, this);
    }

    public static VanillaPathDebugS2CPacket decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

}
