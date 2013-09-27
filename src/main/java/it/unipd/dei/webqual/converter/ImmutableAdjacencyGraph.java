package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.IOException;

public class ImmutableAdjacencyGraph extends ImmutableSequentialGraph {

  /** Length in bytes of the IDs */
  private final int idLen;

  /** The name of the file storing the graph. */
  private final String filename;

  /** The number of nodes of the graph. */
  private final long numNodes;

  private final MinimalPerfectHashFunction<byte[]> map;

  public ImmutableAdjacencyGraph(CharSequence filename, int idLen) {
    this.idLen = idLen;
    this.filename = filename.toString();

    try {
      AdjacencyHeads heads = new AdjacencyHeads(this.filename, idLen);
      ByteArrayTransformationStrategy trStrat = new ByteArrayTransformationStrategy();

      this.map = new MinimalPerfectHashFunction<byte[]>(heads, trStrat);

      this.numNodes = heads.getCount();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long numNodes() {
    return numNodes;
  }
}
