package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.*;
import java.math.BigInteger;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Reads files serialized as immutable graphs.
 *
 * The format of the serialized graph is the following
 *
 *     |head| |adjacency list|
 *
 * Each ID is 64 bits long. To distinguish between the head of an adjacency list
 * and the actual list we use the first bit. If the most significant bit is set to
 * `1`, then the current ID is the head of an adjacency list, otherwise is
 * an element of the adjacency list.
 */
public class ImmutableAdjacencyGraph64 extends ImmutableSequentialGraph {

  /** Length in bytes of the IDs */
  public static final int ID_LEN = 8;

  /** `long` mask with the first bit set */
  public static final long HEAD_MASK_L = 1L << 63;

  /** `long` mask with all the bits other than the most significant set. */
  public static final long RESET_MASK = ~HEAD_MASK_L;

  /** `byte` mask with the most significant bit set. */
  public static final byte HEAD_MASK = (byte) (1 << 7);

  /** The name of the file storing the graph. */
  private final String filename;

  /** The number of nodes of the graph. */
  private final long numNodes;

  /** A map between the original IDs of the graph and IDs in the range `[0, numNodes]` */
  private final Map<Long, Integer> map;

  private ImmutableAdjacencyGraph64( final CharSequence filename ) throws IOException {
    this.filename = filename.toString();
    this.map = new Long2IntOpenHashMap();
    this.numNodes = countNodes();
  }

  protected long countNodes() throws IOException {
    DataInputStream dis = new DataInputStream(
      new BufferedInputStream(new FileInputStream(this.filename)));

    // This is an integer, since we don't deal (for now) with graphs with more
    // than 2^31 nodes
    int cnt = 0;

    int read;
    byte[] buf = new byte[ID_LEN];

    while (true) { // while true: it would be `while (read == 16)` but this way we make only one comparison
      read = dis.read(buf);
      if(read == ID_LEN){
        if(isHead(buf)) {
          long id = reset(new BigInteger(buf).longValue());
          map.put(id, cnt);
          cnt++;
        }
      } else {
        break;
      }
    }

    dis.close();
    if(read != -1) { // -1 means stream exhausted
      throw new IllegalStateException("The last ID was not of " + ID_LEN + " bytes");
    }
    return cnt;
  }

  protected boolean isHead(byte[] id) {
    return (id[0] & HEAD_MASK) == HEAD_MASK;
  }

  protected boolean isHead(long id) {
    return (id & HEAD_MASK_L) == HEAD_MASK_L;
  }

  protected long reset(long id) {
    return id & RESET_MASK;
  }

  protected long resetMap(long id) {
    Integer l = map.get(reset(id));
    if (l == null) {
      return -1;
    } else {
      return l;
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
    return load( basename, (ProgressLogger)null );
  }

  public static ImmutableGraph loadSequential( final CharSequence basename, final ProgressLogger pl ) {
    return load( basename, pl );
  }

  public static ImmutableGraph loadSequential( final CharSequence basename ) {
    return load( basename, (ProgressLogger)null );
  }

  public static ImmutableGraph loadOffline( final CharSequence basename, final ProgressLogger pl ) throws IOException {
    return new ImmutableAdjacencyGraph64( basename );
  }

  public static ImmutableGraph loadOffline( final CharSequence basename ) throws IOException {
    return loadOffline( basename, (ProgressLogger)null );
  }

  public NodeIterator nodeIterator() {
    try {
      return new NodeIterator() {

        final DataInputStream dis = new DataInputStream(
          new BufferedInputStream(new FileInputStream(filename)));
        long outdegree;
        long[][] successors = LongBigArrays.EMPTY_BIG_ARRAY;
        long nextId = resetMap(dis.readLong());

        @Override
        public long nextLong() {
          if(!hasNext()) throw new NoSuchElementException();
          successors = LongBigArrays.ensureCapacity(successors, 10000); // magic number! tweak for efficiency
          outdegree = 0;
          long currentId = nextId;

          try {
            // now read the adjacency
            long neigh = -1;
            while(dis.available() > 0) {
              neigh = dis.readLong();
              // assign the next long if we are on a head
              if(isHead(neigh)) {
                nextId = resetMap(neigh);
                break;
              } else {
                long mapped = resetMap(neigh);
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
