package com.jsorrell.fiberoptics.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

  public static final BlockTest testBlock = new BlockTest();
  public static final Item itemBlockTestBlock = testBlock.createItemBlock();
  public static final BlockOpticalFiber opticalFiber = new BlockOpticalFiber();
  public static final Item itemBlockOpticalFiber = opticalFiber.createItemBlock();

  public static void registerBlocks(IForgeRegistry<Block> registry) {
    registry.registerAll(
            testBlock,
            opticalFiber
    );

    GameRegistry.registerTileEntity(TileOpticalFiber.class, "optical_fiber");
    GameRegistry.registerTileEntity(TileOpticalFiberController.class, "optical_fiber_controller");
  }

  public static void registerItemBlocks(IForgeRegistry<Item> registry) {
    registry.registerAll(
            itemBlockTestBlock,
            itemBlockOpticalFiber
    );
  }

  public static void registerItemBlockModels() {
    testBlock.registerItemBlockModel(Item.getItemFromBlock(testBlock));
    opticalFiber.registerItemBlockModel(Item.getItemFromBlock(opticalFiber));
  }
}
