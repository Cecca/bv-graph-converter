package it.unipd.dei.webqual.converter;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static it.unipd.dei.webqual.converter.Utils.*;

public class AdjacencyHeadIterator implements Iterator<byte[]> {

  private final String fileName;
  private final int idLen;
  private final boolean reset;
  private final DataInputStream dis;

  private boolean hasNext;
  private byte[] next;

  private long count;

  public AdjacencyHeadIterator(String fileName, int idLen, boolean reset) throws IOException {
    this.fileName = fileName;
    this.idLen = idLen;
    this.reset = reset;
    this.dis = new DataInputStream(
      new BufferedInputStream(new FileInputStream(this.fileName)));

    this.hasNext = true;
    byte[] firstNode = new byte[idLen];
    int read = dis.read(firstNode);
    if (read != idLen || !isHead(firstNode)) {
      throw new NoSuchElementException(
        "The first id is not the head of an adjacency list");
    }
    this.next = reset(firstNode);

    this.count = 1;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public byte[] next() {
    final byte[] cur = next;
    // advance to the next head
    try {
      hasNext = false;
      while (dis.available() > 0) {
        byte[] buf = new byte[idLen];
        dis.read(buf);
        if(isHead(buf)) {
          count++;
          if(reset) {
            next = reset(buf);
          } else {
            next = new byte[idLen];
            System.arraycopy(buf, 0, next, 0, idLen);
          }
          hasNext = true;
          break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return cur;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public long getCount() {
    return count;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      dis.close();
    }
    finally {
      super.finalize();
    }
  }

  public static void main(String[] args) throws IOException {
    AdjacencyHeadIterator it = new AdjacencyHeadIterator(
      "links.0", 16, false);

    int cnt = 0;

    while(it.hasNext()) {
      it.next();
      cnt++;
    }

    System.out.printf("Counted: %d\nComputed: %d", cnt, it.getCount());
  }

}
