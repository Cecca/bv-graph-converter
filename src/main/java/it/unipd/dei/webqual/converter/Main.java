package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.*;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.logging.ProgressLogger;
import it.unipd.dei.webqual.converter.merge.GraphMerger;
import it.unipd.dei.webqual.converter.sort.GraphSorter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;

public class Main {

  public static void main(String[] args) throws IOException {
    Options opts = new Options();
    CmdLineParser parser = new CmdLineParser(opts);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.exit(1);
    }

    ProgressLogger pl = new ProgressLogger();
    pl.displayFreeMemory = true;

    pl.logger().info("Building hash function");
    Function<byte[], Long> mapFunc =
      FunctionFactory.buildDeterministicMap(opts.inputGraph, opts.idLen, pl);
    String mphSerializedName = opts.inputGraph + "-mph";
    pl.logger().info("Storing hash function to {}", mphSerializedName);
//    serialize(mphSerializedName, mapFunc);

    pl.logger().info("Loading graph from {}", opts.inputGraph);
    ImmutableGraph originalGraph =
      ImmutableAdjacencyGraph.loadOffline(opts.inputGraph, opts.idLen, mapFunc, pl);

    pl.logger().info("Sorting graph");
    File[] chunks = GraphSorter.splitSorted(originalGraph.nodeIterator(), opts.outputDir, opts.chunkSize);

    pl.logger().info("Merging files");
    File mergedFile;
    if(chunks.length == 1) {
      mergedFile = chunks[0];
    } else {
      mergedFile = GraphMerger.mergeFiles(chunks, opts.outputFile, chunks.length, Long.SIZE / 8, 0);
    }

    pl.start("==== Loading graph from " + mergedFile);
    Function<byte[], Long> map =
      FunctionFactory.buildIdentity(mergedFile.getCanonicalPath(), pl);
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(mergedFile.getCanonicalPath(), 8, map, pl);
    pl.stop("Loaded graph with " + iag.numNodes() + " nodes");

    String efOut = opts.outputFile + "-ef";

    ImmutableGraph efGraph = Conversions.toEFGraph(iag, efOut, pl);

    Checks.checkPositiveIDs(iag, pl);

    pl.logger().info("All done");

    Metrics.report();
    Metrics.reset();
  }

  private static void serialize(String name, Object o) throws IOException {
    ObjectOutputStream oos =
      new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
    oos.writeObject(o);
    oos.close();
  }

  public static class Options {

    @Option(name = "-i", metaVar = "FILE", required = true, usage = "the input graph")
    public String inputGraph;

    @Option(name = "--id-len", metaVar = "N", usage = "the input ID length, defaults to 16")
    public int idLen = 16;

    @Option(name = "-d", metaVar = "OUTPUT_DIR", required = true, usage = "Output directory")
    public File outputDir;

    @Option(name = "-o", metaVar = "FILE", required = true, usage = "Output file")
    public File outputFile;

    @Option(name = "-c", metaVar = "CHUNK_SIZE", usage = "Chunk size for intermediate sorted files. Defaults to 4096")
    public int chunkSize = 300_000;

  }

}
