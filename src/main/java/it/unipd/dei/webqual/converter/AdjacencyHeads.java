package it.unipd.dei.webqual.converter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class AdjacencyHeads implements Iterable<byte[]> {

  private final File file;
  private final int idLen;
  private final AdjacencyHeadIterator.ResetHeads reset;

  private AdjacencyHeadIterator firstIterator;

  public AdjacencyHeads(File file, int idLen) throws IOException {
    this(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
  }

  public AdjacencyHeads(File file, int idLen, AdjacencyHeadIterator.ResetHeads reset) throws IOException {
    this.file = file;
    this.idLen = idLen;
    this.reset = reset;

    this.firstIterator = null;
  }

  public File getFile() {
    return file;
  }

  public int getIdLen() {
    return idLen;
  }

  public long getCount() {
    // completely unwind first iterator
    if (firstIterator == null) {
      try {
        firstIterator = new AdjacencyHeadIterator(file, idLen, reset);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    while(firstIterator.hasNext())
      firstIterator.next();
    return firstIterator.getCount();
  }

  @Override
  public Iterator<byte[]> iterator() {
    try {
      AdjacencyHeadIterator it = new AdjacencyHeadIterator(file, idLen, reset);
      if(firstIterator == null) {
        firstIterator = it;
      }
      return it;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
