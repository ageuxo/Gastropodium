package io.github.ageuxo.Gastropodium.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Path;

public abstract class PathDebugS2CPacket<T extends Path> {
    protected final T path;
    protected final int mobId;
    protected final float maxDistanceToWaypoint;

    public PathDebugS2CPacket(T path, int mobId, float maxDistanceToWaypoint) {
        this.path = path;
        this.mobId = mobId;
        this.maxDistanceToWaypoint = maxDistanceToWaypoint;
    }

/*    public static void handle(PathDebugS2CPacket<?> type, Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->ClientPacketHandler.handleVanillaPathDebugPacket(type)));
        ctx.setPacketHandled(true);
    }*/

    public abstract void encode(FriendlyByteBuf byteBuf);

    public T getPath() {
        return path;
    }

    public int getMobId() {
        return mobId;
    }

    public float getMaxDistanceToWaypoint() {
        return maxDistanceToWaypoint;
    }
}
