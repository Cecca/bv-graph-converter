package it.unipd.dei.webqual.converter;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.mph.HollowTrieDistributorMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.LcpMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Contains factory methods for various types of functions
 */
public class FunctionFactory {

  public static Function<byte[], Long> buildIdentity(File file, ProgressLogger pl) throws IOException {
    AdjacencyHeads heads = new AdjacencyHeads(file, 8); // read longs
    final long size = heads.getCount();
    pl.logger().info("Identity function built on {} nodes", size);

    return new Function<byte[], Long>() {
      @Override
      public Long put(byte[] key, Long value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Long get(Object key) {
        byte[] bytes = (byte[]) key;
        return Utils.reset(Utils.getLong(bytes));
      }

      @Override
      public boolean containsKey(Object key) {
        return false;
      }

      @Override
      public Long remove(Object key) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
        return (int) size;
      }

      @Override
      public void clear() {
        // nothing to do
      }
    };
  }

  public static MinimalPerfectHashFunction<byte[]> buildMphf( File file,
                                                              int idLen,
                                                              ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    MinimalPerfectHashFunction mph =
      new MinimalPerfectHashFunction<byte[]>(heads, trStrat);

    if(mph.size64() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    if(pl != null)
      pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

  public static HollowTrieMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieMonotoneMph( File file,
                                                                                                 int idLen,
                                                                                                 ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    HollowTrieMonotoneMinimalPerfectHashFunction mph =
      new HollowTrieMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);

    if(mph.size64() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    if(pl != null)
      pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

  public static HollowTrieDistributorMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieDistributorMonotoneMph( File file,
                                                                                                 int idLen,
                                                                                                 ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    HollowTrieDistributorMonotoneMinimalPerfectHashFunction mph =
      new HollowTrieDistributorMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);

    if(mph.size64() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    if(pl != null)
      pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

  public static LcpMonotoneMinimalPerfectHashFunction<byte[]> buildLcpMonotoneMph( File file,
                                                                                   int idLen,
                                                                                   ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    LcpMonotoneMinimalPerfectHashFunction mph =
      new LcpMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);

    if(mph.size64() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    if(pl != null)
      pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

  public static ByteArray2LongFunction buildDeterministicMap( File file,
                                                              int idLen,
                                                              ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.displayFreeMemory = true;
    if(pl != null)
      pl.start("Building the deterministic map, with increasing IDs");

    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    ByteArray2LongFunction map = new ByteArray2LongFunction();

    long cnt = 0;
    for(byte[] head : heads) {
      map.put(head, cnt);
      cnt++;
    }

    if(map.size() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    if(pl != null)
      pl.stop("Populated map with " +  map.size() +" elements");

    return map;
  }

}
