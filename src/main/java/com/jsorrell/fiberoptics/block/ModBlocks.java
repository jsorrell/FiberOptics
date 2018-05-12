package com.jsorrell.fiberoptics.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

  public static BlockTest testBlock = new BlockTest();

  public static void registerBlocks(IForgeRegistry<Block> registry) {
    registry.registerAll(
            testBlock
    );
  }

  public static void registerItemBlocks(IForgeRegistry<Item> registry) {
    registry.registerAll(
            testBlock.createItemBlock()
    );
  }

  public static void registerItemBlockModels() {
    testBlock.registerItemBlockModel(Item.getItemFromBlock(testBlock));
  }
}
