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
  public static final BlockOpticalFiberController opticalFiberController = new BlockOpticalFiberController();

  public static void registerBlocks(IForgeRegistry<Block> registry) {
    registry.registerAll(
            testBlock,
            opticalFiber,
            opticalFiberController
    );

    GameRegistry.registerTileEntity(opticalFiber.getTileEntityClass(), opticalFiber.getRegistryName().toString());
    GameRegistry.registerTileEntity(opticalFiberController.getTileEntityClass(), opticalFiberController.getRegistryName().toString());
  }

  public static void registerItemBlocks(IForgeRegistry<Item> registry) {
    registry.registerAll(
            itemBlockTestBlock,
            itemBlockOpticalFiber,
            //FIXME currently being used to hack waila
            opticalFiberController.createItemBlock()
    );
  }

  public static void registerItemBlockModels() {
    testBlock.registerItemBlockModel(Item.getItemFromBlock(testBlock));
    opticalFiber.registerItemBlockModel(Item.getItemFromBlock(opticalFiber));
  }
}
