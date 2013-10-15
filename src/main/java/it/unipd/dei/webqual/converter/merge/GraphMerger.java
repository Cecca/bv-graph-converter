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

  public static void sort(String inPath, String outPath, int idLen) throws IOException {
    List<Pair> pairs = new LinkedList<>();

    // the `true` parameter is for resetting the first bit of the IDs
    LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen);
    while(it.hasNext()) {
      pairs.add(it.next());
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
