package it.unipd.dei.webqual.converter.merge;

import it.unipd.dei.webqual.converter.AdjacencyHeadIterator;
import it.unipd.dei.webqual.converter.Utils;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class takes as input a set of adjacency list files and merges them
 * removing duplicates
 */
public class GraphMerger {

  private static final ArrayComparator ARRAY_COMPARATOR = new ArrayComparator();

  public static class Pair implements Comparable<Pair> {
    byte[] head;
    List<byte[]> neighbours;

    public Pair(byte[] head, List<byte[]> neighbours) {
      this.head = head;
      this.neighbours = neighbours;
    }

    @Override
    public int compareTo(Pair other) {
      return ARRAY_COMPARATOR.compare(this.head, other.head);
    }
  }

  public static void sort(String inPath, String outPath, int idLen) throws IOException {
    List<Pair> pairs = new LinkedList<>();

    // the `true` parameter is for resetting the first bit of the IDs
    AdjacencyHeadIterator it = new AdjacencyHeadIterator(inPath, idLen, true);
    while(it.hasNext()) {
      pairs.add(new Pair(it.next(), it.neighbours()));
    }

    Collections.sort(pairs);
    writePairs(outPath, pairs);
  }

  private static void writePairs(String outPath, List<Pair> pairs)
    throws IOException {

    OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
    for(Pair pair : pairs) {
      out.write(Utils.setHead(pair.head));
      for(byte[] neigh : pair.neighbours) {
        out.write(neigh);
      }
    }
  }

}
