package it.unipd.dei.webqual.converter;

import java.io.IOException;
import java.util.Iterator;

public class AdjacencyHeads implements Iterable<byte[]> {

  private final String fileName;
  private final int idLen;

  private AdjacencyHeadIterator iterator;
  private boolean used;

  public AdjacencyHeads(String fileName, int idLen) throws IOException {
    this.fileName = fileName;
    this.idLen = idLen;

    this.iterator = new AdjacencyHeadIterator(fileName, idLen);
    this.used = false;
  }

  public String getFileName() {
    return fileName;
  }

  public int getIdLen() {
    return idLen;
  }

  public long getCount() {
    return iterator.getCount();
  }

  @Override
  public Iterator<byte[]> iterator() {
    if(used) {
      throw new IllegalStateException(
        "The iterator cannot be used more than once.");
    }
    used = true;
    return iterator;
  }
}
