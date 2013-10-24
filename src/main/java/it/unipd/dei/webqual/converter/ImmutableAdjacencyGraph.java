package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.*;
import java.util.NoSuchElementException;

import static it.unipd.dei.webqual.converter.Utils.isHead;

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

  public NodeIterator nodeIterator() {
    try {
      return new NodeIterator() {

        final DataInputStream dis = new DataInputStream(
          new BufferedInputStream(new FileInputStream(filename)));
        long outdegree;
        long[][] successors = LongBigArrays.EMPTY_BIG_ARRAY;
        long nextId = -1;

        {
          byte[] firstId = new byte[idLen];
          dis.read(firstId);
          if(!isHead(firstId)) {
            throw new NoSuchElementException(
              "The first element of the file is not a head");
          }
          nextId = map.getLong(firstId);
        }

        @Override
        public long nextLong() {
          if(!hasNext()) throw new NoSuchElementException();
          successors = LongBigArrays.ensureCapacity(successors, 10000); // magic number! tweak for efficiency
          outdegree = 0;
          long currentId = nextId;

          try {
            // now read the adjacency
            byte[] buf = new byte[idLen];
            while(dis.available() > 0) {
              dis.read(buf);
              // assign the next long if we are on a head
              if(isHead(buf)) {
                nextId = map.getLong(buf);
                break;
              } else {
                long mapped = map.getLong(buf);
                if(mapped >= 0) {
                  LongBigArrays.set(successors, outdegree++, mapped);
                }
              }
            }

            successors = LongBigArrays.trim(successors, outdegree);

          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          return currentId;
        }

        @Override
        public long[][] successorBigArray() {
          return successors;
        }

        @Override
        public long outdegree() {
          return outdegree;
        }

        @Override
        public boolean hasNext() {
          try {
            return dis.available() > 0;
          } catch (IOException e) {
            return false;
          }
        }

        @Override
        protected void finalize() throws Throwable {
          dis.close();
          super.finalize();
        }
      };
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
