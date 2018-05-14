package com.jsorrell.fiberoptics.client;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class FiberOpticsCreativeTab extends CreativeTabs {

  public static final FiberOpticsCreativeTab creativeTab = new FiberOpticsCreativeTab();

  public FiberOpticsCreativeTab() {
    super(FiberOptics.MODID);
  }

  @Override
  public ItemStack getTabIconItem() {
    return new ItemStack(ModBlocks.itemBlockOpticalFiber);
  }


}
