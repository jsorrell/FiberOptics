package com.jsorrell.fiberoptics.block.optical_fiber;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.properties.PropertyHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PropertyFiberSide extends PropertyHelper<FiberSideType> {

  public PropertyFiberSide(String name) {
    super(name, FiberSideType.class);
  }

  public static PropertyFiberSide create(String name) {
    return new PropertyFiberSide(name);
  }

  @Override
  public Collection<FiberSideType> getAllowedValues() {
    return ImmutableList.copyOf(FiberSideType.values());
  }

  @Override
  public Optional<FiberSideType> parseValue(String value) {
    if ("none".equals(value)) return Optional.of(FiberSideType.NONE);
    if ("self_attachment".equals(value)) return Optional.of(FiberSideType.SELF_ATTACHMENT);
    if ("connection".equals(value)) return Optional.of(FiberSideType.CONNECTION);
    return Optional.absent();
  }

  @Override
  public String getName(FiberSideType value) {
    return value.getName();
  }

}
