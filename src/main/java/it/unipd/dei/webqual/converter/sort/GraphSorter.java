package it.unipd.dei.webqual.converter.sort;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.logging.ProgressLogger;
import it.unipd.dei.webqual.converter.*;
import it.unipd.dei.webqual.converter.merge.GraphMerger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static it.unipd.dei.webqual.converter.Utils.reset;
import static it.unipd.dei.webqual.converter.Utils.setHead;

public class GraphSorter {

  private static ProgressLogger pl = new ProgressLogger();

  private static class Pair implements Comparable<Pair> {
    long head;
    long[][] succs;
    long outDegree;

    public Pair(long head, long[][] succs, long outDegree) {
      this.head = head;
      this.succs = succs;
      this.outDegree = outDegree;
    }

    @Override
    public int compareTo(Pair o) {
      if(this.head < o.head)
        return -1;
      if(this.head > o.head)
        return 1;
      return 0;
    }
  }

  public static File[] splitSorted(NodeIterator it, File baseDir, int numElems) throws IOException {
    ArrayList<File> outputFiles = new ArrayList<>();

    if(!baseDir.isDirectory())
      baseDir.mkdir();

    pl.start("Start splitting the graph in sorted chunks");

    long i = 1;
    List<Pair> chunk = new ArrayList<>(numElems);
    while(it.hasNext()) {
      pl.update();

      long node = it.nextLong();
      long[][] neighs = it.successorBigArray();
      long outdeg = it.outdegree();
      chunk.add(new Pair(node, neighs, outdeg));

      if(i % numElems == 0 || ! it.hasNext()) {
        Collections.sort(chunk);
        File f = writeCollection(chunk, baseDir);
        outputFiles.add(f);
        chunk.clear();
      }
      i++;
    }

    pl.stop("Finish splitting the graph");

    return outputFiles.toArray(new File[outputFiles.size()]);
  }

  private static File writeCollection(Collection<Pair> chunk, File baseDir) throws IOException {
    File out = File.createTempFile("chunk-", "", baseDir);
    pl.logger().info("Writing {}", out.getCanonicalPath());
    DataOutputStream dos =
      new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
    for(Pair p : chunk) {
      dos.writeLong(setHead(p.head));
      long i = p.outDegree;
      LazyLongIterator it = LazyLongIterators.wrap(p.succs);
      while(i-- != 0) {
        dos.writeLong(reset(it.nextLong()));
      }
    }
    dos.close();
    Checks.checkDuplicates(out, 8, pl);
    pl.logger().info("Write completed");
    return out;
  }

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

    pl.logger().info("Building hash function");
    Function<byte[], Long> mapFunc =
      FunctionFactory.buildMphf(opts.inputGraph, opts.idLen, pl);
    String mphSerializedName = opts.inputGraph + "-mph";
    pl.logger().info("Storing hash function to {}", mphSerializedName);
    serialize(mphSerializedName, mapFunc);

    pl.logger().info("Loading graph from {}", opts.inputGraph);
    ImmutableGraph originalGraph =
      ImmutableAdjacencyGraph.loadOffline(opts.inputGraph, opts.idLen, mapFunc, pl);

    pl.logger().info("Sorting graph");
    File[] chunks = splitSorted(originalGraph.nodeIterator(), opts.outputDir, opts.chunkSize);

    pl.logger().info("Merging files");
    File mergedFile;
    if(chunks.length == 1) {
      mergedFile = chunks[0];
    } else {
      mergedFile = GraphMerger.mergeFiles(chunks, opts.outputFile, chunks.length, Long.SIZE/8, 0);
    }

    pl.start("==== Loading graph from " + opts.outputFile);
    Function<byte[], Long> map =
      FunctionFactory.buildIdentity(mergedFile.getName(), pl);
    ImmutableGraph iag =
      ImmutableAdjacencyGraph.loadOffline(mergedFile.getName(), 8, map, pl);
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
