package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.logging.ProgressLogger;
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

  private final ProgressLogger pl;

  private ImmutableAdjacencyGraph(CharSequence filename, int idLen, ProgressLogger pl) {
    this.idLen = idLen;
    this.filename = filename.toString();
    this.pl = pl;

    try {
      pl.start("Counting nodes and creating perfect hash function");
      AdjacencyHeads heads = new AdjacencyHeads(this.filename, idLen);
      ByteArrayTransformationStrategy trStrat =
        new ByteArrayTransformationStrategy(idLen);

      this.map = new MinimalPerfectHashFunction<byte[]>(heads, trStrat);

      this.numNodes = heads.getCount();

      pl.logger().info("Loaded graph with {} nodes.", this.numNodes);
      pl.logger().info("The perfect hash function is using {} bits", map.numBits());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long numNodes() {
    return numNodes;
  }

  public static ImmutableGraph load( final CharSequence basename, final ProgressLogger pl ) {
    throw new UnsupportedOperationException( "Graphs may be loaded offline only" );
  }

  public static ImmutableGraph load( final CharSequence basename ) {
    return load( basename, null );
  }

  public static ImmutableGraph loadSequential( final CharSequence basename, final ProgressLogger pl ) {
    return load( basename, pl );
  }

  public static ImmutableGraph loadSequential( final CharSequence basename ) {
    return load( basename, null );
  }

  public static ImmutableGraph loadOffline( final CharSequence basename, final int idLen, final ProgressLogger pl ) throws IOException {
    return new ImmutableAdjacencyGraph( basename, idLen, pl );
  }

  public static ImmutableGraph loadOffline( final CharSequence basename ) throws IOException {
    return loadOffline( basename, 16, null );
  }

  public static void main(String[] args) throws IOException {
    ImmutableAdjacencyGraph.loadOffline("links.0", 16, new ProgressLogger());
  }

}
