package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberClient;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberController;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

  public static final BlockOpticalFiber opticalFiber = new BlockOpticalFiber();
  public static final Item itemBlockOpticalFiber = opticalFiber.createItemBlock();

  public static void registerBlocks(IForgeRegistry<Block> registry) {
    registry.registerAll(
            opticalFiber
    );

    GameRegistry.registerTileEntity(TileOpticalFiber.class, "optical_fiber");
    GameRegistry.registerTileEntity(TileOpticalFiberController.class, "optical_fiber_controller");
    GameRegistry.registerTileEntity(TileOpticalFiberClient.class, "optical_fiber_client");
  }

  public static void registerItemBlocks(IForgeRegistry<Item> registry) {
    registry.registerAll(
            itemBlockOpticalFiber
    );
  }

  public static void registerItemBlockModels() {
    opticalFiber.registerItemBlockModel(Item.getItemFromBlock(opticalFiber));
  }
}
