package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.*;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: bv-graph-converter links-file out-basename");
      System.exit(1);
    }

    ProgressLogger pl = new ProgressLogger();

    String inputFile = args[0];
    String outBasename = args[1];

    pl.logger().info("==== Loading graph from " + inputFile);
    Function<byte[], Long> map =
      FunctionFactory.buildDeterministicMap(inputFile, 16, pl);
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(inputFile, 16, map, pl);
    pl.logger().info("Loaded graph with " + iag.numNodes() + " nodes");

    String efOut = outBasename + "-ef";
    pl.logger().info(
      "==== Converting the graph to Elias-Fano format: output " + efOut);
    EFGraph.store(iag, efOut, pl);

    ImmutableGraph efGraph = EFGraph.loadOffline(efOut);

    pl.logger().info("==== Statistics ====");
    pl.logger().info("Number of nodes: " + efGraph.numNodes());
    pl.logger().info("Number of arcs: " + efGraph.numArcs());

    pl.logger().info("==== Checking for errors ====");
    if(!iag.equals(efGraph)) {
      pl.logger().error("Graphs are not equal!!!");
      pl.logger().info(efGraph.numNodes() + " ?= " + iag.numNodes());
      checkSuccessors((ImmutableAdjacencyGraph) iag, (EFGraph) efGraph);
    }
  }

  public static void checkForOutOfRange(ImmutableGraph g, ProgressLogger pl) {
    NodeIterator ni = g.nodeIterator();
    while(ni.hasNext()) {
      long node = ni.next();
      LazyLongIterator succs = ni.successors();
      long outDegree = ni.outdegree();
      while(outDegree-- > 0) {
        long succ = succs.nextLong();
        if(succ < 0 || succ > g.numNodes()) {
          throw new RuntimeException(
            String.format("Out of bounds neighbour: %d of node %d", succ, node));
        }
      }
    }
    pl.logger().info("Check completed, no errors found");
  }

  public static void checkSuccessors(ImmutableAdjacencyGraph a, EFGraph b) {
    long totNodes = a.numNodes();
    NodeIterator ai = a.nodeIterator();
    LazyLongIterator succA, succB;
    long outDegA, outDegB;
    while(ai.hasNext()) {
      long node = ai.next();
      succA = ai.successors();
      outDegA = ai.outdegree();
      succB = b.successors(node);
      outDegB = b.outdegree(node);
      if (outDegA != outDegB) {
        throw new RuntimeException(
          "Outdegree of node " + node + " is different in the two graphs: IAG="+outDegA+" EFGraph="+outDegB);
      }
    }
  }

}
