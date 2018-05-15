package com.jsorrell.fiberoptics.connection;

import javax.annotation.Nullable;

interface OpticalFiberOutput {
  @Nullable
  Object getServing();
}
