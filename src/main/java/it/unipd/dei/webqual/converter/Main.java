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

    pl.start("==== Loading graph from " + inputFile);
    Function<byte[], Long> map =
      FunctionFactory.buildDeterministicMap(inputFile, 16, pl);
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(inputFile, 16, map, pl);
    pl.stop("Loaded graph with " + iag.numNodes() + " nodes");

    String efOut = outBasename + "-ef";
    pl.start(
      "==== Converting the graph to Elias-Fano format: output " + efOut);
    EFGraph.store(iag, efOut, pl);
    pl.stop("Conversion completed");

    ImmutableGraph efGraph = EFGraph.loadOffline(efOut);

    Statistics.stats(efGraph, pl);

    pl.start("==== Checking for errors ====");
    if(!iag.equals(efGraph)) {
      pl.logger().error("Graphs are not equal!!!");
      pl.logger().info(efGraph.numNodes() + " ?= " + iag.numNodes());
    }
    pl.stop("Check completed, no errors found :-)");
  }

}
