package io.github.ageuxo.Gastropodium;

import io.github.ageuxo.Gastropodium.entity.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GastropodiumMod.MODID)
public class GastropodiumMod {
    public static final String MODID = "gastropodium";

    public GastropodiumMod(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.register(modEventBus);

    }

    public static ResourceLocation modRL(String path){
        return new ResourceLocation(MODID, path);
    }
}