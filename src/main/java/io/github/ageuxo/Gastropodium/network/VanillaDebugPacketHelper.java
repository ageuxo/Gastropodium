package io.github.ageuxo.Gastropodium.network;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgePath;
import io.github.ageuxo.Gastropodium.mixins.PathAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class VanillaDebugPacketHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<Node> NODE_CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.INT.fieldOf("x").forGetter((n)->n.x),
            Codec.INT.fieldOf("y").forGetter((n)->n.y),
            Codec.INT.fieldOf("z").forGetter((n)->n.z),
            Codec.FLOAT.fieldOf("walkedDistance").forGetter(node -> node.walkedDistance),
            Codec.FLOAT.fieldOf("costMalus").forGetter(node -> node.costMalus),
            Codec.BOOL.fieldOf("closed").forGetter(node -> node.closed),
            Codec.STRING.fieldOf("type").forGetter(node -> node.type.name()),
            Codec.FLOAT.fieldOf("f").forGetter(node -> node.f)
    ).apply(instance, VanillaDebugPacketHelper::nodeFromCodec));

    public static Codec<Path> PATH_CODEC = RecordCodecBuilder.create(instance->instance.group(
            NODE_CODEC.listOf().fieldOf("nodes").forGetter(path->((PathAccessor)path).getNodes()),
            BlockPos.CODEC.fieldOf("target").forGetter(net.minecraft.world.level.pathfinder.Path::getTarget),
            Codec.BOOL.fieldOf("reached").forGetter(net.minecraft.world.level.pathfinder.Path::canReach),
            Codec.INT.fieldOf("nextNode").forGetter(net.minecraft.world.level.pathfinder.Path::getNextNodeIndex),
            NODE_CODEC.listOf().fieldOf("open").forGetter(path -> Arrays.stream(path.getOpenSet()).toList()),
            NODE_CODEC.listOf().fieldOf("closed").forGetter(path -> Arrays.stream(path.getOpenSet()).toList())
    ).apply(instance, VanillaDebugPacketHelper::pathFromCodec));

    public static void sendVanillaPathfindingPacket(Level level, Mob mob, Path path, float maxDistanceToWaypoint){
        if (!level.isClientSide && path != null ){
//            logDebugInfo(mob.getId(), path);
            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> mob),
                    new VanillaPathDebugS2CPacket(path, mob.getId(), maxDistanceToWaypoint));
        }
    }

    public static void sendBlockEdgePathfindingPacket(Level level, Mob mob, BlockEdgePath path, float maxDistanceToWaypoint){
        if (!level.isClientSide && path != null){
            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> mob),
                    new BlockEdgePathDebugS2CPacket(path, mob.getId(), maxDistanceToWaypoint));
        }
    }

    public static Node nodeFromCodec(int x, int y, int z, float walkDistance, float costMalus, boolean closed, String typeName, float f){
        Node node = new Node(x, y, z);
        node.walkedDistance = walkDistance;
        node.costMalus = costMalus;
        node.closed = closed;
        node.type = BlockPathTypes.valueOf(typeName);
        node.f = f;
        return node;
    }

    public static Path pathFromCodec(List<Node> nodes, BlockPos target, boolean reached, int nextNode, List<Node> openSet, List<Node> closedSet){
        Path path = new Path(nodes, target, reached);
        ((PathAccessor)path).callSetDebug(openSet.toArray(Node[]::new), closedSet.toArray(Node[]::new), null);
        path.setNextNodeIndex(nextNode);
        return path;
    }

    public static void logDebugInfo(int mobId, Path path) {
        LOGGER.debug("{}, {}, {}, {}, {}, {}", mobId, path, path.getTarget(), path.getNode(0), path.getNode(1), path.getNode(2));
    }
}
