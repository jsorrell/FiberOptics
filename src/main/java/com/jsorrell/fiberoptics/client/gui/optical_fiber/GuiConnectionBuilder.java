package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;

public abstract class GuiConnectionBuilder extends GuiOpticalFiber {
  protected final OpticalFiberConnectionFactory connectionFactory;
  public GuiConnectionBuilder(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory.pos);
    this.connectionFactory = connectionFactory;
  }
}
