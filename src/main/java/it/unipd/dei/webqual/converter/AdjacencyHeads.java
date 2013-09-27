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

  public static void main(String[] args) throws IOException {
    AdjacencyHeads hs = new AdjacencyHeads("links.0", 16);
    long cnt = 0;
    for(byte[] h : hs) {
      cnt++;
    }

    assert(cnt == hs.getCount());
    System.out.printf("Counted: %d\nComputed: %d", cnt, hs.getCount());
  }

}
