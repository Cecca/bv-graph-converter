package it.unipd.dei.webqual.converter.merge;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PairMerger implements Merger<Pair> {

  private final Comparator<byte[]> arrayComparator;
  private static final ArrayMerger ARRAY_MERGER = new ArrayMerger();

  public PairMerger(Comparator<byte[]> arrayComparator) {
    this.arrayComparator = arrayComparator;
  }

  @Override
  public Pair merge(Pair first, Pair second) {
    assert(arrayComparator.compare(first.head, second.head) == 0);
    List<byte[]>
      firstNeighs = first.neighbours,
      secondNeighs = second.neighbours;

    Collections.sort(firstNeighs, arrayComparator);
    Collections.sort(secondNeighs, arrayComparator);

    LazyMergeIterator<byte[]> it =
      new LazyMergeIterator<byte[]>(
        firstNeighs.iterator(), secondNeighs.iterator(), arrayComparator, ARRAY_MERGER);

    List<byte[]> merged = new LinkedList<>();
    while(it.hasNext()) {
      merged.add(it.next());
    }

    return new Pair(first.head, merged, arrayComparator);
  }

  private static class ArrayMerger implements Merger<byte[]> {
    @Override
    public byte[] merge(byte[] first, byte[] second) {
      return first;
    }
  }

}
