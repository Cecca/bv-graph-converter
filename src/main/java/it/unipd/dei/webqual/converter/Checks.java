package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.logging.ProgressLogger;

public class Checks {

  public static boolean checkEquality(ImmutableGraph a, ImmutableGraph b, ProgressLogger pl) {
    pl.start("==== Checking for equality ====");
    if(!a.equals(b)) {
      pl.logger().error("Graphs are not equal!!!");
      pl.logger().info(a.numNodes() + " ?= " + b.numNodes());
      return false;
    }
    pl.stop("Check completed, no errors found, the graphs are equals :-)");
    return true;
  }

  public static boolean checkPositiveIDs(ImmutableGraph g, ProgressLogger pl) {
    pl.start("==== Checking for positive IDs ====");
    NodeIterator it = g.nodeIterator();
    while(it.hasNext()) {
      pl.update();
      long node = it.next();
      if(node < 0) {
        pl.logger().error("ID {} is negative", node);
        pl.stop();
        return false;
      }
      long outdeg = it.outdegree();
      LazyLongIterator succs = it.successors();
      while(outdeg-- != 0) {
        long s = succs.nextLong();
        if(s < 0) {
          pl.logger().error("ID {}, neighbour of {}, is negative", s, node);
          pl.stop();
          return false;
        }
      }
    }
    pl.stop("Check completed, no errors found, the graph has only positive IDs :-)");
    return true;
  }

}
