package it.unipd.dei.webqual.converter;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class ImmutableAdjacencyGraph64 extends ImmutableSequentialGraph {

  public static final int ID_LEN = 8;

  public static final long HEAD_MASK_L = 1L << 63;

  public static final long RESET_MASK = ~HEAD_MASK_L;

  public static final byte HEAD_MASK = (byte) (1 << 7);

  private final String filename;

  private final long numNodes;

  private ImmutableAdjacencyGraph64( final CharSequence filename ) throws IOException {
    this.filename = filename.toString();
    this.numNodes = countNodes();
  }

  protected long countNodes() throws IOException {
    DataInputStream dis = new DataInputStream(new FileInputStream(this.filename));

    long cnt = 0;
    int read;
    byte[] buf = new byte[ID_LEN];

    while (true) { // while true: it would be `while (read == 16)` but this way we make only one comparison
      read = dis.read(buf);
      if(read == ID_LEN){
        if(isHead(buf)) {
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

        final DataInputStream dis = new DataInputStream(new FileInputStream(filename));
        long outdegree;
        long[][] successors = LongBigArrays.EMPTY_BIG_ARRAY;
        long nextId = reset(dis.readLong());

        @Override
        public long nextLong() {
          if(!hasNext()) throw new NoSuchElementException();
          successors = LongBigArrays.ensureCapacity(successors, 10); // magic number! tweak for efficiency
          outdegree = 0;
          long currentId = nextId;

          try {
            // now read the adjacency
            long neigh = -1;
            while(dis.available() > 0) {
              neigh = dis.readLong();
              // assign the next long if we are on a head
              if(isHead(neigh)) {
                nextId = reset(neigh);
                break;
              } else {
                LongBigArrays.set(successors, outdegree++, reset(neigh));
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
