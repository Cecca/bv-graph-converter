package it.unipd.dei.webqual.converter;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class ByteArray2LongFunction implements Function<byte[], Long> {

  private static final int DEFAULT_RET_VALUE = -1;

  private Long2IntOpenHashMap map;

  public ByteArray2LongFunction() {
    super();
    this.map = new Long2IntOpenHashMap();
    this.map.defaultReturnValue(DEFAULT_RET_VALUE);
  }

  private static long transform(byte[] bytes) {
    return Utils.getLong(Utils.reset(bytes));
  }

  @Override
  public Long put(byte[] bytes, Long aLong) {
    final long oldValue = map.put(transform(bytes), aLong.intValue());
    if (oldValue != DEFAULT_RET_VALUE) {
      throw new RuntimeException(
        "The input sequence contains duplicates: " + aLong + " has " + oldValue + " already associated");
    }
    return oldValue;
  }

  @Override
  public Long get(Object o) {
    return (long) map.get(transform((byte[]) o));
  }

  @Override
  public boolean containsKey(Object o) {
    return map.containsKey(transform((byte[]) o));
  }

  @Override
  public Long remove(Object o) {
    return (long) map.remove(transform((byte[]) o));
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public void clear() {
    map.clear();
  }
}
