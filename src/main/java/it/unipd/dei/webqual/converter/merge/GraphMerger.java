package it.unipd.dei.webqual.converter.merge;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import it.unipd.dei.webqual.converter.AdjacencyHeadIterator;
import it.unipd.dei.webqual.converter.Utils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class takes as input a set of adjacency list files and merges them
 * removing duplicates
 */
public class GraphMerger {

  private static final Logger log = LoggerFactory.getLogger(GraphMerger.class);

  private static final ExecutorService executor =
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public static final MetricRegistry metrics = new MetricRegistry();

  private static final PairComparator PAIR_COMPARATOR = new PairComparator();
  private static final PairMerger PAIR_MERGER = new PairMerger();

  private static void sort(String inPath, String outPath, int idLen) throws IOException {
    log.info("Sorting {}", inPath);
    Timer timer = metrics.timer("file-sorting");
    Timer.Context context = timer.time();
    List<Pair> pairs = new LinkedList<>();

    // the `true` parameter is for resetting the first bit of the IDs
    LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen);
    while(it.hasNext()) {
      pairs.add(it.next());
    }

    Collections.sort(pairs);
    writePairs(new File(outPath), pairs);
    long time = context.stop();
    log.info("{} sorted, elapsed time: {} seconds", inPath, time / 1000000000);
  }

  private static void writeIterator(File outPath, Iterator<Pair> pairs) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
    while(pairs.hasNext()) {
      Pair pair = pairs.next();
      out.write(Utils.setHead(pair.head));
      for(byte[] neigh : pair.neighbours) {
        out.write(neigh);
      }
    }
    out.close();
  }

  private static void writePairs(File outPath, List<Pair> pairs)
    throws IOException {
    writeIterator(outPath, pairs.iterator());
  }

  private static File[] sortFiles(File[] inFiles, int idLen) throws IOException {
    log.info("============= Sorting files ===============");
    Timer timer = metrics.timer("total-sorting");
    Timer.Context context = timer.time();
    File[] sortedFiles = new File[inFiles.length];
    for(int i = 0; i<inFiles.length; i++) {
      sortedFiles[i] = File.createTempFile("graph-merger", "sorting"+inFiles[i].getName());
      sort(inFiles[i].getCanonicalPath(), sortedFiles[i].getCanonicalPath(), idLen);
    }
    long time = context.stop();
    log.info("====== Files sorted, elapsed time: {} seconds", time / 1000000000);
    return sortedFiles;
  }

  /**
   * Asychronously merges files
   * @param sortedFiles
   * @param outputName
   * @param groupBy
   * @param idLen
   * @param recursionLevel
   * @return
   * @throws IOException
   */
  private static Future<File> mergeFiles( final File[] sortedFiles,
                                          final File outputName,
                                          final int groupBy,
                                          final int idLen,
                                          final int recursionLevel) throws IOException {
    if(groupBy < 2) {
      throw new IllegalArgumentException("groupBy should be >= 2");
    }
    if(sortedFiles.length <= groupBy) {
      log.info("Recursion level {}. Merging {} files.",
        recursionLevel, sortedFiles.length);
      return
        executor.submit(new Callable<File>() {
          @Override
          public File call() throws Exception {
            return mergeFiles(sortedFiles, outputName, idLen);
          }
        });
    }

    File[] tmpFiles = new File[sortedFiles.length / groupBy];
    Future<File>[] tmpFutures = new Future[tmpFiles.length];

    for(int i = 0; i < tmpFiles.length; i++) {
      tmpFiles[i] = File.createTempFile("graph-merger", "merging");

      File[] group = Arrays.copyOfRange(
        sortedFiles, i*groupBy, Math.min((i + 1)*groupBy, sortedFiles.length));

      log.info("Recursion level {}. Merging {} out of {} files.",
        recursionLevel, group.length, sortedFiles.length);
      tmpFutures[i] = mergeFiles(group, tmpFiles[i], groupBy, idLen, recursionLevel+1);
    }

    for (int i = 0; i < tmpFutures.length; i++) {
      try {
        log.info("Recursion level {}. Completed partial merging: {}%",
          recursionLevel, (((double) i)/tmpFiles.length)*100);
        tmpFiles[i] = tmpFutures[i].get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    log.info("Recursion level {}. Merging temporary files together", recursionLevel);
    return mergeFiles(tmpFiles, outputName, groupBy, idLen, recursionLevel+1);
  }

  /**
   * Creates a single lazy iterator over the merged files and uses it to create
   * the real merge file.
   * @param sortedFiles
   * @param output
   */
  private static File mergeFiles(File[] sortedFiles, File output, int idLen) throws IOException {
    Timer timer = metrics.timer("file-merging");
    Timer.Context context = timer.time();
    LazyMergeIterator<Pair>[] iterators = new LazyMergeIterator[sortedFiles.length/2];
    for(int i=0; i<iterators.length; i++) {
      if((2*i+1) < sortedFiles.length) {
        iterators[i] = new LazyMergeIterator<>(
          new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), idLen),
          new LazyFilePairIterator(sortedFiles[2*i+1].getCanonicalPath(), idLen),
          PAIR_COMPARATOR, PAIR_MERGER);
      } else {
        iterators[i] = new LazyMergeIterator<>(
          new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), idLen),
          PAIR_COMPARATOR, PAIR_MERGER);
      }
    }

    LazyMergeIterator<Pair> it = LazyMergeIterator.compose(PAIR_COMPARATOR, PAIR_MERGER, iterators);
    writeIterator(output, it);

    long time = context.stop();
    log.info("{} files merged, elapsed time: {} seconds", sortedFiles.length, time/1000000000);

    return output;
  }

  private static void checkFiles(File[] sortedFiles, int idLen) throws IOException {
    for(int i=0; i<sortedFiles.length; i++) {
      System.out.printf("%f%%\r", ((double) i)/sortedFiles.length * 100);
      checkFile(sortedFiles[i], idLen);
    }
  }

  private static void checkFile(File f, int idLen) throws IOException {
    ArrayComparator cmp = new ArrayComparator();
    AdjacencyHeadIterator it =
      new AdjacencyHeadIterator(f.getCanonicalPath(), idLen, true);
    byte[] last = it.next();
    while(it.hasNext()) {
      byte[] cur = it.next();
      if(cmp.compare(cur, last) < 0) {
        throw new IllegalArgumentException("File " + f + " is not sorted");
      }
      last = cur;
    }
  }

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

    Options opts = new Options();
    CmdLineParser parser = new CmdLineParser(opts);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.exit(1);
    }

    File[] inFiles = opts.inputDir.listFiles();

    File[] sortedFiles = (opts.noSort)? inFiles : sortFiles(inFiles, opts.idLen);

    if(!opts.noCheck) {
      log.info("Checking sorted files");
      checkFiles(sortedFiles, opts.idLen);
    }

    log.info("============= Merging files ===============");
    Timer timer = metrics.timer("total-merging");
    Timer.Context context = timer.time();
    Future<File> mergeFuture =
      mergeFiles(sortedFiles, new File(opts.outputName), opts.groupBy, opts.idLen, 0);
    mergeFuture.get();
    long time = context.stop();
    log.info("====== Files merged, elapsed time: {} seconds", time/1000000000);


    log.info("{} duplicates have been merged", metrics.counter("duplicates").getCount());

    CsvReporter reporter = CsvReporter.forRegistry(metrics)
                                      .formatFor(Locale.ITALY)
                                      .convertRatesTo(TimeUnit.SECONDS)
                                      .convertDurationsTo(TimeUnit.SECONDS)
                                      .build(new File("."));
    reporter.report();
    reporter.close();
  }

  public static class Options {

    @Option(aliases="--input-dir", name="-i", usage="The input directory", required=true, metaVar="FILE")
    File inputDir;

    @Option(aliases="--out", name="-o", usage="The output file", required=true, metaVar="FILE")
    String outputName;

    @Option(aliases="--group-by", name="-g", usage="Merge files in chunks of N files", metaVar="N")
    int groupBy = 2;

    @Option(aliases="--id-len", name="-l", usage="The length of the IDs in bytes", metaVar="N")
    int idLen = 16;

    @Option(name="--no-sort", usage="Skip sorting phase. Input must be already sorted")
    boolean noSort = false;

    @Option(name="--no-check", usage="Skip checking of input files")
    boolean noCheck = false;

  }

}
