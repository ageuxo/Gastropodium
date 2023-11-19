package io.github.ageuxo.Gastropodium.mixins;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public interface PathAccessor {
    @Invoker
    void callSetDebug(Node[] pOpenSet, Node[] pClosedSet, Set<Target> pTargetNodes);

    @Accessor
    List<Node> getNodes();

    @Accessor
    Node[] getOpenSet();

    @Accessor
    Node[] getClosedSet();

    @Accessor
    Set<Target> getTargetNodes();

    @Accessor
    int getNextNodeIndex();
}
