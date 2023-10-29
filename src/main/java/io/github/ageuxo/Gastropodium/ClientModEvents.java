package io.github.ageuxo.Gastropodium;

import io.github.ageuxo.Gastropodium.entity.ModEntities;
import io.github.ageuxo.Gastropodium.entity.client.BasicSlugModel;
import io.github.ageuxo.Gastropodium.entity.client.BasicSlugRenderer;
import io.github.ageuxo.Gastropodium.entity.client.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GastropodiumMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(ModelLayers.SLUG_LAYER, BasicSlugModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
        EntityRenderers.register(ModEntities.BASIC_SLUG.get(), BasicSlugRenderer::new);
    }
}
