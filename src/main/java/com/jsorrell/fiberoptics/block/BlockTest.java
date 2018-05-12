package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockTest extends BlockBase {
  public BlockTest() {
    super(Material.ROCK, "test_block");

    setHardness(3f);
    setResistance(5f);
  }

  @Override
  public BlockTest setCreativeTab(CreativeTabs tab) {
    super.setCreativeTab(tab);
    return this;
  }
}
