package it.unipd.dei.webqual.converter.merge;

import java.util.Comparator;

public class PairComparator implements Comparator<Pair> {
  @Override
  public int compare(Pair pair1, Pair pair2) {
    return pair1.compareTo(pair2);
  }
}
