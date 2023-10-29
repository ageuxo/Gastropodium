package io.github.ageuxo.Gastropodium;

import io.github.ageuxo.Gastropodium.entity.BasicSlugEntity;
import io.github.ageuxo.Gastropodium.entity.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GastropodiumMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.BASIC_SLUG.get(), BasicSlugEntity.createAttributes().build());
    }
}
