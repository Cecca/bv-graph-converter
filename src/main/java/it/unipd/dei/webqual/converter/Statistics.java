package it.unipd.dei.webqual.converter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Statistics {

  public static void stats(ImmutableGraph graph, ProgressLogger pl) {
    pl.logger().info("==== Statistics ====");
    pl.logger().info("Number of nodes: " + graph.numNodes());
    pl.logger().info("Number of arcs: " + graph.numArcs());
    MetricRegistry iagRegistry = ImmutableAdjacencyGraph.getRegistry();
    Counter missingItems = iagRegistry.counter(
        ImmutableAdjacencyGraph.missingItemsCounterName(graph.basename().toString(), 0));
    Histogram degreesDist = iagRegistry.histogram(
        ImmutableAdjacencyGraph.degreeHistogramName(graph.basename().toString(), 0));
    pl.logger().info("Mean outdegree: {}", degreesDist.getSnapshot().getMean());
    pl.logger().info("Max outdegree: {}", degreesDist.getSnapshot().getMax());
    pl.logger().info("Missing IDs: {}", missingItems.getCount());

    File reportsDir = new File("reports");

    CsvReporter reporter = CsvReporter.forRegistry(iagRegistry)
      .formatFor(Locale.ITALIAN)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.SECONDS)
      .build(reportsDir);
    if(!reportsDir.isDirectory())
      reportsDir.mkdir();
    reporter.report();
    reporter.close();
  }

}
