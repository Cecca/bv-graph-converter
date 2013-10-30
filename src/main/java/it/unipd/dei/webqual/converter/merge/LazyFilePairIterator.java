package it.unipd.dei.webqual.converter.merge;

import it.unipd.dei.webqual.converter.AdjacencyHeadIterator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class LazyFilePairIterator implements Iterator<Pair> {

  private AdjacencyHeadIterator innerIterator;

  public LazyFilePairIterator(File file, int idLen) throws IOException {
    this.innerIterator = new AdjacencyHeadIterator(file, idLen, AdjacencyHeadIterator.ResetHeads.RESET);
  }

  @Override
  public boolean hasNext() {
    return innerIterator.hasNext();
  }

  @Override
  public Pair next() {
    return new Pair(innerIterator.next(), innerIterator.neighbours());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
