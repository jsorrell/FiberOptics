package com.jsorrell.fiberoptics.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

  public static BlockTest testBlock = new BlockTest();
  public static BlockOpticalFiber opticalFiber = new BlockOpticalFiber();

  public static void registerBlocks(IForgeRegistry<Block> registry) {
    registry.registerAll(
            testBlock,
            opticalFiber
    );
  }

  public static void registerItemBlocks(IForgeRegistry<Item> registry) {
    registry.registerAll(
            testBlock.createItemBlock(),
            opticalFiber.createItemBlock()
    );
  }

  public static void registerItemBlockModels() {
    testBlock.registerItemBlockModel(Item.getItemFromBlock(testBlock));
    opticalFiber.registerItemBlockModel(Item.getItemFromBlock(opticalFiber));
  }
}
