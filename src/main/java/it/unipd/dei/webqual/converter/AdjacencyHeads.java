package it.unipd.dei.webqual.converter;

import java.io.IOException;
import java.util.Iterator;

public class AdjacencyHeads implements Iterable<byte[]> {

  private final String fileName;
  private final int idLen;

  private AdjacencyHeadIterator firstIterator;

  public AdjacencyHeads(String fileName, int idLen) throws IOException {
    this.fileName = fileName;
    this.idLen = idLen;

    this.firstIterator = null;
  }

  public String getFileName() {
    return fileName;
  }

  public int getIdLen() {
    return idLen;
  }

  public long getCount() {
    // completely unwind first iterator
    if (firstIterator == null) {
      try {
        firstIterator = new AdjacencyHeadIterator(fileName, idLen);
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
      AdjacencyHeadIterator it = new AdjacencyHeadIterator(fileName, idLen);
      if(firstIterator == null) {
        firstIterator = it;
      }
      return it;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
