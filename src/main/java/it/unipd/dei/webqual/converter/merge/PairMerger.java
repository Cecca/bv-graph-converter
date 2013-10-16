package it.unipd.dei.webqual.converter.merge;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PairMerger implements Merger<Pair> {

  private static final ArrayComparator ARRAY_COMPARATOR = new ArrayComparator();
  private static final ArrayMerger ARRAY_MERGER = new ArrayMerger();

  @Override
  public Pair merge(Pair first, Pair second) {
    assert(ARRAY_COMPARATOR.compare(first.head, second.head) == 0);
    List<byte[]>
      firstNeighs = first.neighbours,
      secondNeighs = second.neighbours;

    Collections.sort(firstNeighs, ARRAY_COMPARATOR);
    Collections.sort(secondNeighs, ARRAY_COMPARATOR);

    LazyMergeIterator<byte[]> it =
      new LazyMergeIterator<byte[]>(
        firstNeighs.iterator(), secondNeighs.iterator(), ARRAY_COMPARATOR, ARRAY_MERGER);

    List<byte[]> merged = new LinkedList<>();
    while(it.hasNext()) {
      merged.add(it.next());
    }

    return new Pair(first.head, merged);
  }

  private static class ArrayMerger implements Merger<byte[]> {
    @Override
    public byte[] merge(byte[] first, byte[] second) {
      return first;
    }
  }

}
