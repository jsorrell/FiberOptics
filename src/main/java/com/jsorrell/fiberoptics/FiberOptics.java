package com.jsorrell.fiberoptics;

import com.jsorrell.fiberoptics.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = FiberOptics.MODID, name = FiberOptics.NAME, version = FiberOptics.VERSION, useMetadata = true)
public class FiberOptics {
  public static final String MODID = "fiberoptics";
  public static final String NAME = "Fiber Optics";
  public static final String VERSION = "1.0";

  @SidedProxy(serverSide = "com.jsorrell.fiberoptics.CommonProxy", clientSide = "com.jsorrell.fiberoptics.ClientProxy")
  public static CommonProxy proxy;

  @Mod.Instance
  public static FiberOptics instance;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent e) {
    //NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent e) {
    //ModidPacketHandler.registerPacketHandlers();
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent e) {
  }

  @Mod.EventBusSubscriber
  public static class RegistrationHandler {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
      ModBlocks.registerItemBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
      ModBlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
      ModBlocks.registerItemBlockModels();
    }
  }
}
