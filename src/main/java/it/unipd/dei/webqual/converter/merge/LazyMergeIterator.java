package it.unipd.dei.webqual.converter.merge;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates lazily over the sequence obtained as the merge of two given
 * sequences
 */
public class LazyMergeIterator<T extends Comparable<T>> implements Iterator<T> {

  private Iterator<T> first;
  private Iterator<T> second;

  private Merger<T> merger;

  private T firstNext;
  private T secondNext;

  public LazyMergeIterator(
    Iterator<T> first, Iterator<T> second, Merger<T> merger) {

    this.first = first;
    this.second = second;

    this.merger = merger;

    this.firstNext = (first.hasNext())? first.next() : null;
    this.secondNext = (second.hasNext())? second.next() : null;

  }

  @Override
  public boolean hasNext() {
    return firstNext != null || secondNext != null;
  }

  private T getAndNextFirst() {
    T result = firstNext;
    this.firstNext = (first.hasNext())? first.next() : null;
    return result;
  }

  private T getAndNextSecond() {
    T result = secondNext;
    this.secondNext = (second.hasNext())? second.next() : null;
    return result;
  }

  @Override
  public T next() {
    if(firstNext == null && secondNext != null) {
      return getAndNextSecond();
    } else if (firstNext != null && secondNext == null) {
      return getAndNextFirst();
    } else if (firstNext == null && secondNext == null) {
      throw new NoSuchElementException();
    }

    int res = firstNext.compareTo(secondNext);

    if(res < 0) {
      return getAndNextFirst();
    } else if (res > 0) {
      return getAndNextSecond();
    }

    // if the elements are equals we should merge them, along with all the
    // subsequent equals elements
    T merged = merger.merge(getAndNextFirst(), getAndNextSecond());

    while (firstNext != null && merged.compareTo(firstNext) == 0) {
      merged = merger.merge(merged, getAndNextFirst());
    }
    while (firstNext != null && merged.compareTo(secondNext) == 0) {
      merged = merger.merge(merged, getAndNextSecond());
    }

    return merged;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
