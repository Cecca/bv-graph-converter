package it.unipd.dei.webqual.converter;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.IOException;

/**
 * Contains factory methods for variuous types of maps
 */
public class MapFactory {

  public static MinimalPerfectHashFunction<byte[]> buildMphf( String file,
                                                              int idLen,
                                                              ProgressLogger pl) throws IOException {
    pl.start("Counting nodes and creating perfect hash function");
    AdjacencyHeads heads = new AdjacencyHeads(file, idLen);
    ByteArrayTransformationStrategy trStrat =
      new ByteArrayTransformationStrategy(idLen);

    MinimalPerfectHashFunction mph =
      new MinimalPerfectHashFunction<byte[]>(heads, trStrat);

    pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
    return mph;
  }

}
