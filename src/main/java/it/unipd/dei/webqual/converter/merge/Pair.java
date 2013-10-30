package it.unipd.dei.webqual.converter.merge;

import java.util.Comparator;
import java.util.List;

public class Pair implements Comparable<Pair> {

  private final Comparator<byte[]> arrayComparator;

  byte[] head;
  List<byte[]> neighbours;

  public Pair(byte[] head, List<byte[]> neighbours, Comparator<byte[]> comparator) {
    this.head = head;
    this.neighbours = neighbours;
    this.arrayComparator = comparator;
  }

  @Override
  public int compareTo(Pair other) {
    return arrayComparator.compare(this.head, other.head);
  }

}
