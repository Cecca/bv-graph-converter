package it.unipd.dei.webqual.converter;

import java.io.*;
import java.util.*;

import static it.unipd.dei.webqual.converter.Utils.*;

public class AdjacencyHeadIterator implements Iterator<byte[]> {

  public static enum ResetHeads { RESET, DONT_RESET }

  private final File file;
  private final int idLen;
  private final ResetHeads reset;
  private final DataInputStream dis;

  private boolean hasNext;
  private byte[] next;

  private List<byte[]> neighbours;

  private long count;

  public AdjacencyHeadIterator(File file, int idLen, ResetHeads reset) throws IOException {
    this.file = file;
    this.idLen = idLen;
    this.reset = reset;
    this.dis = new DataInputStream(
      new BufferedInputStream(new FileInputStream(this.file)));

    this.hasNext = true;
    byte[] firstNode = new byte[idLen];
    int read = dis.read(firstNode);
    if (read != idLen || !isHead(firstNode)) {
      throw new NoSuchElementException(
        "The first id is not the head of an adjacency list: file " + file);
    }
    switch(reset){
      case RESET:
        this.next = reset(firstNode);
        break;
      case DONT_RESET:
        this.next = new byte[idLen];
        System.arraycopy(firstNode, 0, next, 0, idLen);
        break;
    }

    this.count = 1;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  public List<byte[]> neighbours() {
    return neighbours;
  }

  @Override
  public byte[] next() {
    final byte[] cur = next;
    // advance to the next head
    try {
      hasNext = false;
      neighbours = new ArrayList<>();
      while (dis.available() > 0) {
        byte[] buf = new byte[idLen];
        dis.read(buf);
        if(isHead(buf)) {
          count++;
          switch(reset) {
            case RESET:
              next = reset(buf);
              break;
            case DONT_RESET:
              next = new byte[idLen];
              System.arraycopy(buf, 0, next, 0, idLen);
              break;
          }
          hasNext = true;
          break;
        } else {
          neighbours.add(buf);
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

}
