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

    String inputFile = args[0];
    String outBasename = args[1];

    System.out.println("==== Loading graph from " + inputFile);
    Function<byte[], Long> map =
      FunctionFactory.buildLcpMonotoneMph(inputFile, 16, new ProgressLogger());
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(inputFile, 16, map, new ProgressLogger());
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

    System.out.println("==== Checking for errors ====");
//    checkForOutOfRange(efGraph);

    if(!iag.equals(efGraph)) {
      System.out.println("Graphs are not equal!!!");
      System.out.println(efGraph.numNodes() + " ?= " + iag.numNodes());
      checkSuccessors((ImmutableAdjacencyGraph) iag, (EFGraph) efGraph);
    }
  }

  public static void checkForOutOfRange(ImmutableGraph g) {
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
