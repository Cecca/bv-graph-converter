package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.EFGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;

public class Conversions {

  public static ImmutableGraph toEFGraph(ImmutableGraph g, String outName, ProgressLogger pl) throws IOException {
    pl.start("==== Converting the graph to Elias-Fano format: output " + outName);
    EFGraph.store(g, outName, pl);
    pl.stop("Conversion completed");
    return EFGraph.loadOffline(outName, pl);
  }

  public static ImmutableGraph toBVGraph(ImmutableGraph g, String outName, ProgressLogger pl) throws IOException {
    pl.start("==== Converting the graph to Boldi-Vigna format: output " + outName);
    BVGraph.store(g, outName, pl);
    pl.stop("Conversion completed");
    return BVGraph.loadOffline(outName, pl);
  }

}
