package it.unipd.dei.webqual.converter;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.IOException;

/**
 * Contains factory methods for various types of functions
 */
public class FunctionFactory {

  public static MinimalPerfectHashFunction<byte[]> buildMphf( String file,
                                                              int idLen,
                                                              ProgressLogger pl) throws IOException {
    pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    MinimalPerfectHashFunction mph =
      new MinimalPerfectHashFunction<byte[]>(heads, trStrat);

    if(mph.size64() != heads.getCount()) {
      throw new IllegalStateException(
        "Number of elements counted by the iterator is different than the number " +
          "of elements counted by the  minimal perfect hash function");
    }

    pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

}
