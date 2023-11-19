package io.github.ageuxo.Gastropodium.entity.pathing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import org.jetbrains.annotations.NotNull;

public class BlockEdgeNode extends Node{
    protected final int x;
    protected final int y;
    protected final int z;
    public final Direction edge;
    public BlockEdgeNode cameFrom;
    public static Codec<BlockEdgeNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(BlockEdgeNode::getX),
            Codec.INT.fieldOf("y").forGetter(BlockEdgeNode::getY),
            Codec.INT.fieldOf("z").forGetter(BlockEdgeNode::getZ),
            Direction.CODEC.fieldOf("edge").forGetter(BlockEdgeNode::getEdge),
            Codec.STRING.fieldOf("type").forGetter(BlockEdgeNode::getTypeName),
            Codec.FLOAT.fieldOf("walkedDistance").forGetter(edgeNode -> edgeNode.walkedDistance),
            Codec.FLOAT.fieldOf("costMalus").forGetter(edgeNode -> edgeNode.costMalus),
            Codec.BOOL.fieldOf("closed").forGetter(edgeNode -> edgeNode.closed),
            Codec.FLOAT.fieldOf("f").forGetter(edgeNode -> edgeNode.f)
    ).apply(instance, BlockEdgeNode::new));

    public BlockEdgeNode(int x, int y, int z, Direction edge) {
        super(x,y,z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.edge = edge;
    }

    public BlockEdgeNode(int x, int y, int z, Direction edge, String typeName, float walkedDistance, float costMalus, boolean closed, float f) {
        super(x,y,z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.edge = edge;
        this.type = BlockPathTypes.valueOf(typeName);
        this.walkedDistance = walkedDistance;
        this.costMalus = costMalus;
        this.closed = closed;
        this.f = f;
    }

    public BlockEdgeNode(BlockPos pos, Direction edge){
        this(pos.getX(), pos.getY(), pos.getZ(), edge);
    }

    @Override
    public void writeToStream(FriendlyByteBuf pBuffer) {
        pBuffer.writeInt(this.x);
        pBuffer.writeInt(this.y);
        pBuffer.writeInt(this.z);
        pBuffer.writeEnum(this.edge);
        pBuffer.writeFloat(this.walkedDistance);
        pBuffer.writeFloat(this.costMalus);
        pBuffer.writeBoolean(this.closed);
        pBuffer.writeEnum(this.type);
        pBuffer.writeFloat(this.f);
    }

    @Override
    public @NotNull String toString() {
        return "BlockEdgeNode{" + "x=" + x + ", y=" + y + ", z=" + z + ", edge=" + edge + '}';
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Direction getEdge() {
        return edge;
    }

    public String getTypeName(){
        return this.type.name();
    }
}
