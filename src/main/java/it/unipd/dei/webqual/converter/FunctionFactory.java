package it.unipd.dei.webqual.converter;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.mph.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

/**
 * Contains factory methods for various types of functions
 */
public class FunctionFactory {

  public static MinimalPerfectHashFunction<byte[]> buildMphf( String file,
                                                              int idLen,
                                                              ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
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

  public static HollowTrieMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieMonotoneMph( String file,
                                                                                                 int idLen,
                                                                                                 ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
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

  public static HollowTrieDistributorMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieDistributorMonotoneMph( String file,
                                                                                                 int idLen,
                                                                                                 ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
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

  public static LcpMonotoneMinimalPerfectHashFunction<byte[]> buildLcpMonotoneMph( String file,
                                                                                   int idLen,
                                                                                   ProgressLogger pl) throws IOException {
    if(pl != null)
      pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
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

}
