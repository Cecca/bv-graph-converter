package it.unipd.dei.webqual.converter;

import it.unimi.dsi.logging.ProgressLogger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;

public class MapWriter {

  private static String bytesToHexString(byte[] bs) {
    StringBuffer s = new StringBuffer();
    for(byte b : bs) {
      s.append(String.format("%02X", b));
    }
    return s.toString();
  }

  public static void main(String[] args) throws IOException {
    Options opts = new Options();
    CmdLineParser parser = new CmdLineParser(opts);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.exit(1);
    }

    ProgressLogger pl = new ProgressLogger();

    File input = opts.inputGraph;
    int idLen = opts.idLen;
    File output = opts.outputFile;

    PrintWriter out = new PrintWriter(
      new BufferedOutputStream(new FileOutputStream(output)));

    AdjacencyHeadIterator heads =
      new AdjacencyHeadIterator(input, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
    int cnt = 0;
    pl.start("Building map");
    while(heads.hasNext()) {
      out.println(bytesToHexString(heads.next()) + " " + cnt++);
      pl.update();
    }
    pl.stop();
    out.close();
  }

  public static class Options {

    @Option(name = "-i", metaVar = "FILE", required = true, usage = "the input graph")
    public File inputGraph;

    @Option(name = "--id-len", metaVar = "N", usage = "the input ID length, defaults to 16")
    public int idLen = 16;

    @Option(name = "-o", metaVar = "FILE", required = true, usage = "Output file")
    public File outputFile;

  }

}
