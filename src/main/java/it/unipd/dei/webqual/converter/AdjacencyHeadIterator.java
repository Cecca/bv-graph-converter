package it.unipd.dei.webqual.converter;

import java.io.*;
import java.util.Iterator;

public class AdjacencyHeadIterator implements Iterator<byte[]> {

  private final String fileName;
  private final int idLen;
  private final DataInputStream dis;

  private boolean hasNext;
  private byte[] next;


  public AdjacencyHeadIterator(String fileName, int idLen) throws FileNotFoundException {
    this.fileName = fileName;
    this.idLen = idLen;
    this.dis = new DataInputStream(
      new BufferedInputStream(new FileInputStream(this.fileName)));
  }

  @Override
  public boolean hasNext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] next() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
