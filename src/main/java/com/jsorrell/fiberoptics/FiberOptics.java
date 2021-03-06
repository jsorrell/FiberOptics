package com.jsorrell.fiberoptics;

import com.jsorrell.fiberoptics.block.ModBlocks;
import com.jsorrell.fiberoptics.fiber_network.type.ModTransferTypes;
import com.jsorrell.fiberoptics.item.ModItems;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.logging.Logger;

@Mod(modid = FiberOptics.MODID, name = FiberOptics.NAME, version = FiberOptics.VERSION, useMetadata = true)
public class FiberOptics {
  public static final String MODID = "fiberoptics";
  public static final String NAME = "Fiber Optics";
  public static final String VERSION = "1.0";

  public static Logger LOGGER = Logger.getLogger(MODID);

  @SidedProxy(serverSide = "com.jsorrell.fiberoptics.CommonProxy", clientSide = "com.jsorrell.fiberoptics.ClientProxy")
  public static CommonProxy proxy;

  @Mod.Instance
  @SuppressWarnings("unused")
  public static FiberOptics instance;

  @Mod.EventHandler
  @SuppressWarnings("unused")
  public void preInit(FMLPreInitializationEvent e) {
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new FiberOpticsGuiHandler());

  }

  @Mod.EventHandler
  @SuppressWarnings("unused")
  public void init(FMLInitializationEvent e) {
    FiberOpticsPacketHandler.registerPacketHandlers();
    ModTransferTypes.registerTransferTypes();
  }

  @Mod.EventHandler
  @SuppressWarnings("unused")
  public void postInit(FMLPostInitializationEvent e) {
  }

  @Mod.EventBusSubscriber
  public static class RegistrationHandler {
    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void registerItems(RegistryEvent.Register<Item> event) {
      ModBlocks.registerItemBlocks(event.getRegistry());
      ModItems.registerItems(event.getRegistry());
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
      ModBlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void registerModels(ModelRegistryEvent event) {
      ModBlocks.registerItemBlockModels();
      ModItems.registerItemModels();
    }
  }
}
