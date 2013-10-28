package it.unipd.dei.webqual.converter;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class ByteArray2LongFunction implements Function<byte[], Long> {

  private Long2LongOpenHashMap map = new Long2LongOpenHashMap();

  @Override
  public Long put(byte[] bytes, Long aLong) {
    final long id = Utils.getLong(bytes);
    final Long oldValue = map.put(id, (long) aLong);
    if (oldValue != null) {
      throw new RuntimeException("The input sequence contains duplicates");
    }
    return oldValue;
  }

  @Override
  public Long get(Object o) {
    return map.get(Utils.getLong((byte[]) o));
  }

  @Override
  public boolean containsKey(Object o) {
    return map.containsKey(Utils.getLong((byte[]) o));
  }

  @Override
  public Long remove(Object o) {
    return map.remove(Utils.getLong((byte[]) o));
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
