package it.unipd.dei.webqual.converter.merge;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import it.unipd.dei.webqual.converter.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class takes as input a set of adjacency list files and merges them
 * removing duplicates
 */
public class GraphMerger {

  private static final Logger log = LoggerFactory.getLogger(GraphMerger.class);

  public static final MetricRegistry metrics = new MetricRegistry();

  private static final int GROUP_BY = 4;
  private static final int ID_LEN = 16;
  private static final PairComparator PAIR_COMPARATOR = new PairComparator();
  private static final PairMerger PAIR_MERGER = new PairMerger();

  private static void sort(String inPath, String outPath, int idLen) throws IOException {
    log.info("Sorting {}", inPath);
    Timer timer = metrics.timer("File sorting");
    Timer.Context context = timer.time();
    List<Pair> pairs = new LinkedList<>();

    // the `true` parameter is for resetting the first bit of the IDs
    LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen);
    while(it.hasNext()) {
      pairs.add(it.next());
    }

    Collections.sort(pairs);
    writePairs(outPath, pairs);
    long time = context.stop();
    log.info("{} sorted, elapsed time: {} seconds", inPath, time/1000000000);
  }

  private static void writePairs(String outPath, List<Pair> pairs)
    throws IOException {

    OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
    for(Pair pair : pairs) {
      out.write(Utils.setHead(pair.head));
      for(byte[] neigh : pair.neighbours) {
        out.write(neigh);
      }
    }
    out.close();
  }

  private static File[] sortFiles(File[] inFiles) throws IOException {
    log.info("============= Sorting files ===============");
    Timer timer = metrics.timer("Total sorting");
    Timer.Context context = timer.time();
    File[] sortedFiles = new File[inFiles.length];
    for(int i = 0; i<inFiles.length; i++) {
      sortedFiles[i] = File.createTempFile("graph-merger", "sorting"+inFiles[i].getName());
      sort(inFiles[i].getCanonicalPath(), sortedFiles[i].getCanonicalPath(), ID_LEN);
    }
    long time = context.stop();
    log.info("====== Files sorted, elapsed time: {} seconds", time / 1000000000);
    return sortedFiles;
  }

  private static void mergeFiles(File[] sortedFiles, String outputName, int groupBy) throws IOException {
    if(groupBy < 2) {
      throw new IllegalArgumentException("groupBy should be >= 2");
    }
    log.info("============= Merging files ===============");
    Timer timer = metrics.timer("Total merging");
    Timer.Context context = timer.time();
    if(sortedFiles.length <= groupBy) {
      mergeFiles(sortedFiles, outputName);
    }

    File[] tmpFiles = new File[sortedFiles.length / groupBy];

    for(int i = 0; i < tmpFiles.length; i++) {
      tmpFiles[i] = File.createTempFile("graph-merger", "merging");

      File[] group = Arrays.copyOfRange(
        sortedFiles, i*groupBy, Math.min((i + 1)*groupBy, sortedFiles.length));

      mergeFiles(group, tmpFiles[i].getCanonicalPath());
    }
    long time = context.stop();
    log.info("====== Files merged, elapsed time: {} seconds", time/1000000000);
  }

  /**
   * Creates a single lazy iterator over the merged files and uses it to create
   * the real merge file.
   * @param sortedFiles
   * @param outputName
   */
  private static void mergeFiles(File[] sortedFiles, String outputName) throws IOException {
    log.info("Merging {}", Arrays.toString(sortedFiles));
    Timer timer = metrics.timer("File merging");
    Timer.Context context = timer.time();
    LazyMergeIterator<Pair>[] iterators = new LazyMergeIterator[sortedFiles.length/2];
    for(int i=0; i<iterators.length; i++) {
      if((2*i+1) < sortedFiles.length) {
        iterators[i] = new LazyMergeIterator<>(
          new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), ID_LEN),
          new LazyFilePairIterator(sortedFiles[2*i+1].getCanonicalPath(), ID_LEN),
          PAIR_COMPARATOR, PAIR_MERGER);
      } else {
        iterators[i] = new LazyMergeIterator<>(
          new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), ID_LEN),
          PAIR_COMPARATOR, PAIR_MERGER);
      }
    }

    LazyMergeIterator<Pair> it = LazyMergeIterator.compose(PAIR_COMPARATOR, PAIR_MERGER, iterators);

    OutputStream out = new BufferedOutputStream(new FileOutputStream(outputName));
    while(it.hasNext()) {
      Pair pair = it.next();
      out.write(Utils.setHead(pair.head));
      for(byte[] neigh : pair.neighbours) {
        out.write(neigh);
      }
    }
    out.close();
    long time = context.stop();
    log.info("{} files merged, elapsed time: {} seconds", sortedFiles.length, time/1000000000);
  }

  public static void main(String[] args) throws IOException {

    if(args.length != 2) {
      log.error("USAGE: graph-merger input_dir output_file");
      System.exit(1);
    }

    String inputDir = args[0];
    String outputName = args[1];

    File[] inFiles = new File(inputDir).listFiles();

    File[] sortedFiles = sortFiles(inFiles);

    mergeFiles(sortedFiles, outputName, GROUP_BY);

    CsvReporter reporter = CsvReporter.forRegistry(metrics)
                                      .formatFor(Locale.ITALY)
                                      .convertRatesTo(TimeUnit.SECONDS)
                                      .convertDurationsTo(TimeUnit.SECONDS)
                                      .build(new File("."));
    reporter.report();
  }

}
