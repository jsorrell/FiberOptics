package com.jsorrell.fiberoptics.util;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

public enum Axis2D {
  X("X") {
   @Override
    public double getRelevantSize(Dimension2D dim) {
      return dim.getWidth();
    }

    @Override
    public double getRelevantCoordinate(double x, double y) {
      return x;
    }
  },
  Y("Y") {
    @Override
    public double getRelevantSize(Dimension2D dim) {
      return dim.getHeight();
    }

    @Override
    public double getRelevantCoordinate(double x, double y) {
      return y;
    }
  };

  public final String name;

  Axis2D(String name) {
    this.name = name;
  }

  public double getRelevantCoordinate(Point2D p) {
    return getRelevantCoordinate(p.getX(), p.getY());
  }
  public int getRelevantCoordinate(Point p) {
    return getRelevantCoordinate(p.x, p.y);
  }
  public abstract double getRelevantSize(Dimension2D dim);
  public int getRelevantSize(Dimension dim) {
    return (int) getRelevantSize((Dimension2D) dim);
  }
  public abstract double getRelevantCoordinate(double x, double y);
  public int getRelevantCoordinate(int x, int y) {
    return (int) getRelevantCoordinate((double) x, (double) y);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
