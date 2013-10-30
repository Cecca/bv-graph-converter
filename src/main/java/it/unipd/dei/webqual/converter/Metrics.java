package it.unipd.dei.webqual.converter;

import ch.qos.logback.classic.turbo.MatchingFilter;
import com.codahale.metrics.*;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Metrics {

  public static final MetricRegistry registry = new MetricRegistry();

  public static final Counter duplicatesCounter = registry.counter("duplicates");

  public static final Timer fileSortingTimer = registry.timer("file-sorting");

  public static final Timer totalSortingTimer = registry.timer("total-sorting");

  public static final Timer fileMergingTimer = registry.timer("file-merging");

  public static final Timer totalMergingTimer = registry.timer("total-merging");

  public static Counter missingItemsCounter(String fileName, int iteratorId) {
    return registry.counter(missingItemsCounterName(fileName, iteratorId));
  }

  public static Histogram degreeHistogram(String fileName, int iteratorId) {
    return registry.histogram(degreeHistogramName(fileName, iteratorId));
  }

  public static void report() {
    File reportsDir = new File("reports");
    CsvReporter reporter = CsvReporter.forRegistry(registry)
      .formatFor(Locale.ITALY)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.SECONDS)
      .build(reportsDir);
    if(!reportsDir.isDirectory())
      reportsDir.mkdir();
    reporter.report();
    reporter.close();

    ConsoleReporter console = ConsoleReporter.forRegistry(registry)
      .formattedFor(Locale.ITALY)
      .convertDurationsTo(TimeUnit.SECONDS)
      .convertRatesTo(TimeUnit.SECONDS)
      .build();
    console.report();
    console.stop();
    console.close();
  }

  public static void reset() {
    registry.removeMatching(MetricFilter.ALL);
  }

  public static String missingItemsCounterName(String filename, int id) {
    return "iag-"+filename+"-missing-items-"+id;
  }

  public static String degreeHistogramName(String filename, int id) {
    return "iag-"+filename+"-degree-dist-"+id;
  }
}
