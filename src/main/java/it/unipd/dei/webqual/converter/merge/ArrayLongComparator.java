package it.unipd.dei.webqual.converter.merge;

import it.unipd.dei.webqual.converter.Utils;

import java.util.Comparator;

public class ArrayLongComparator implements Comparator<byte[]> {
  @Override
  public int compare(byte[] o1, byte[] o2) {
    final long a = Utils.reset(Utils.getLong(o1));
    final long b = Utils.reset(Utils.getLong(o2));
    if(a < b)
      return -1;
    if(a > b)
      return 1;
    return 0;
  }
}
