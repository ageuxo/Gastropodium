package io.github.ageuxo.Gastropodium.entity;

import io.github.ageuxo.Gastropodium.GastropodiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, GastropodiumMod.MODID);

    public static final RegistryObject<EntityType<BasicSlugEntity>> BASIC_SLUG = ENTITY_TYPES.register("basic_slug",
            ()-> EntityType.Builder.of(BasicSlugEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f)
                    .build("basic_slug"));

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
