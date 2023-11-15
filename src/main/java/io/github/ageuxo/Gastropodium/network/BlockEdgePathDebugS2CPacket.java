package io.github.ageuxo.Gastropodium.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgePath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlockEdgePathDebugS2CPacket {
    private final BlockEdgePath path;
    private final Mob mob;
    private final float maxDistanceToWaypoint;
    public static Codec<BlockEdgePathDebugS2CPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockEdgePath.CODEC.fieldOf("path").forGetter(BlockEdgePathDebugS2CPacket::getPath),
            Codec.INT.fieldOf("id").forGetter(BlockEdgePathDebugS2CPacket::getMobId),
            Codec.FLOAT.fieldOf("maxDistanceToWaypoint").forGetter(BlockEdgePathDebugS2CPacket::getMaxDistanceToWaypoint)
    ).apply(instance, BlockEdgePathDebugS2CPacket::new));

    public BlockEdgePathDebugS2CPacket(BlockEdgePath path,  int mobId, float maxDistanceToWaypoint) {
        this(path, Minecraft.getInstance().level, mobId, maxDistanceToWaypoint);
    }

    public BlockEdgePathDebugS2CPacket(BlockEdgePath path, Level level, int mobId, float maxDistanceToWaypoint) {
        this.path = path;
        this.mob = (Mob) level.getEntity(mobId);
        this.maxDistanceToWaypoint = maxDistanceToWaypoint;
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

    public BlockEdgePath getPath() {
        return path;
    }

    public int getMobId() {
        return mob.getId();
    }

    public float getMaxDistanceToWaypoint() {
        return maxDistanceToWaypoint;
    }
}
