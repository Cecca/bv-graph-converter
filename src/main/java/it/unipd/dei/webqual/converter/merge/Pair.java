package it.unipd.dei.webqual.converter.merge;

import java.util.List;

public class Pair implements Comparable<Pair> {

  private static final ArrayComparator ARRAY_COMPARATOR = new ArrayComparator();

  byte[] head;
  List<byte[]> neighbours;

  public Pair(byte[] head, List<byte[]> neighbours) {
    this.head = head;
    this.neighbours = neighbours;
  }

  @Override
  public int compareTo(Pair other) {
    return ARRAY_COMPARATOR.compare(this.head, other.head);
  }

}
