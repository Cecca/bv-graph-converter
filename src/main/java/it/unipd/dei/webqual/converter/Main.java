package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.EFGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
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
      ImmutableAdjacencyGraph128.loadOffline(inputFile);
    System.out.println("Loaded graph with " + iag.numNodes() + " nodes");


    ProgressLogger pl = new ProgressLogger();

    String efOut = outBasename + "-ef";
    System.out.println(
      "==== Converting the graph to Elias-Fano format: output " + efOut);
    EFGraph.store(iag, efOut, pl);

    pl = new ProgressLogger();
    String bvOut = outBasename + "-bv";
    ImmutableGraph efGraph = EFGraph.load(efOut);

    System.out.println("==== Statistics ====");
    System.out.println("Number of nodes: " + efGraph.numNodes());
    System.out.println("Number of arcs: " + efGraph.numArcs());

    System.out.println(
      "==== Converting the graph to Boldi-Vigna format: output " + bvOut);
    BVGraph.store(efGraph, bvOut, pl);

  }

}
