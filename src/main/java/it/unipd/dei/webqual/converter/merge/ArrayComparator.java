package it.unipd.dei.webqual.converter.merge;

import java.util.Comparator;

public class ArrayComparator implements Comparator<byte[]> {

  @Override
  public int compare(byte[] first, byte[] second) {
    int n = Math.min(first.length, second.length);
    int i=0;
    for (; i < n; i++) {
      if (first[i] > second[i]) {
        return 1;
      } else if (first[i] < second[i]) {
        return -1;
      }
    }

    if(first.length > second.length) {
      return 1;
    } else if (first.length < second.length) {
      return -1;
    } else {
      return 0;
    }

  }

}
