package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.FiberOpticsCreativeTab;
import net.minecraft.item.Item;

public class ItemBase extends Item {
  protected String name;

  public ItemBase(String name) {
    this.name = name;
    setUnlocalizedName(name);
    setRegistryName(name);
    setCreativeTab(FiberOpticsCreativeTab.creativeTab);
  }

  public void registerItemModel() {
    FiberOptics.proxy.registerItemRenderer(this, 0, name);
  }
}
