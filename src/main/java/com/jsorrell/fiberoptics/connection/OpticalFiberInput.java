package com.jsorrell.fiberoptics.connection;


import javax.annotation.Nonnull;

interface OpticalFiberInput {
  int canAccept(@Nonnull Object o);
}
