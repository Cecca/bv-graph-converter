package it.unipd.dei.webqual.converter;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.bits.TransformationStrategy;
import static it.unipd.dei.webqual.converter.Utils.*;

public class MD5TransformationStrategy implements TransformationStrategy<byte[]> {

  public static final int NUM_BITS = 128;

  public static final int HASH_LEN = 16;

  public MD5TransformationStrategy() {
    // nothing to do
  }

  @Override
  public TransformationStrategy<byte[]> copy() {
    return new MD5TransformationStrategy();
  }

  @Override
  public BitVector toBitVector(byte[] bytes) {
    long l1=0, l2=0;

    l1 = (l1 << 8) + (bytes[0] & 0xff);
    l1 = (l1 << 8) + (bytes[1] & 0xff);
    l1 = (l1 << 8) + (bytes[2] & 0xff);
    l1 = (l1 << 8) + (bytes[3] & 0xff);
    l1 = (l1 << 8) + (bytes[4] & 0xff);
    l1 = (l1 << 8) + (bytes[5] & 0xff);
    l1 = (l1 << 8) + (bytes[6] & 0xff);
    l1 = (l1 << 8) + (bytes[7] & 0xff);

    l2 = (l2 << 8) + (bytes[8] & 0xff);
    l2 = (l2 << 8) + (bytes[9] & 0xff);
    l2 = (l2 << 8) + (bytes[10] & 0xff);
    l2 = (l2 << 8) + (bytes[11] & 0xff);
    l2 = (l2 << 8) + (bytes[12] & 0xff);
    l2 = (l2 << 8) + (bytes[13] & 0xff);
    l2 = (l2 << 8) + (bytes[14] & 0xff);
    l2 = (l2 << 8) + (bytes[15] & 0xff);

    long[] la = {l1, l2};

    return LongArrayBitVector.wrap(la);
  }

  @Override
  public long numBits() {
    return NUM_BITS;
  }

  @Override
  public long length(byte[] bytes) {
    return NUM_BITS;
  }
}
