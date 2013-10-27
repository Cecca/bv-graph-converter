package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.*;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: bv-graph-converter links-file out-basename");
      System.exit(1);
    }

    String inputFile = args[0];
    String outBasename = args[1];

    System.out.println("==== Loading graph from " + inputFile);
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(inputFile, 16, new ProgressLogger());
    System.out.println("Loaded graph with " + iag.numNodes() + " nodes");


    ProgressLogger pl = new ProgressLogger();

    String efOut = outBasename + "-ef";
    System.out.println(
      "==== Converting the graph to Elias-Fano format: output " + efOut);
    EFGraph.store(iag, efOut, pl);

    ImmutableGraph efGraph = EFGraph.loadOffline(efOut);

    System.out.println("==== Statistics ====");
    System.out.println("Number of nodes: " + efGraph.numNodes());
    System.out.println("Number of arcs: " + efGraph.numArcs());

//    String bvOut = outBasename + "-bv";
//    System.out.println(
//      "==== Converting the graph to Boldi-Vigna format: output " + bvOut);
//    BVGraph.store(iag, bvOut, pl);
//
//    ImmutableGraph bvGraph = BVGraph.loadOffline(bvOut);
//
//    System.out.println("==== Statistics ====");
//    System.out.println("Number of nodes: " + bvGraph.numNodes());
//    System.out.println("Number of arcs: " + bvGraph.numArcs());

    checkForOutOfRange(iag);
  }

  public static void checkForOutOfRange(ImmutableGraph g) {
    System.out.println("==== Checking for errors ====");
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
    System.out.println("Check completed, no errors found");
  }

}
