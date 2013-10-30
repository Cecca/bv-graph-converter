package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.logging.ProgressLogger;

public class Checks {

  public static void checkEquality(ImmutableGraph a, ImmutableGraph b, ProgressLogger pl) {
    pl.start("==== Checking for equality ====");
    if(!a.equals(b)) {
      pl.logger().error("Graphs are not equal!!!");
      pl.logger().info(a.numNodes() + " ?= " + b.numNodes());
    }
    pl.stop("Check completed, no errors found, the graphs are equals :-)");
  }

}
