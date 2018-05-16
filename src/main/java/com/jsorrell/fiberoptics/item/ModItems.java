package com.jsorrell.fiberoptics.item;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

  public static final Terminator terminator = new Terminator();
  public static final ItemDebugTool debugTool = new ItemDebugTool();

  public static void registerItems(IForgeRegistry<Item> registry) {
    registry.registerAll(
            terminator,
            debugTool
    );
  }

  public static void registerItemModels() {
    debugTool.registerItemModel();
    terminator.registerItemModel();
  }
}
