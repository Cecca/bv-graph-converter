package it.unipd.dei.webqual.converter.sort;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.logging.ProgressLogger;
import it.unipd.dei.webqual.converter.Checks;

import java.io.*;
import java.util.*;

import static it.unipd.dei.webqual.converter.Utils.reset;
import static it.unipd.dei.webqual.converter.Utils.setHead;

public class GraphSorter {

  private static HashSet<Long> alreadySeen = new HashSet<>();

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

      if(alreadySeen.contains(node))
        throw new RuntimeException("Node "+node+" has already been see. (Position "+i+")");
      alreadySeen.add(node);

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

}
