package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.item.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block implements IHasModel {
  protected String name;

  public BlockBase(Material material, String name) {
    super(material);
    this.name = name;
    setUnlocalizedName(name);
    setRegistryName(name);
    //setCreativeTab(TutorialMod.creativeTab); //this creative tab hasn't been implemented yet
  }

  @Override
  public void registerItemBlockModel(Item itemBlock) {
    FiberOptics.proxy.registerItemRenderer(itemBlock, 0, name);
  }

  @Override
  public BlockBase setCreativeTab(CreativeTabs tab) {
    super.setCreativeTab(tab);
    return this;
  }

  public Item createItemBlock() {
    return new ItemBlock(this).setRegistryName(name);
  }
}
