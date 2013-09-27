package it.unipd.dei.webqual.converter;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.bits.TransformationStrategy;

public class ByteArrayTransformationStrategy implements TransformationStrategy<byte[]> {

  private final int numBytes;

  public ByteArrayTransformationStrategy(int numBytes) {
    this.numBytes = numBytes;
  }

  @Override
  public TransformationStrategy<byte[]> copy() {
    return new ByteArrayTransformationStrategy(this.numBytes);
  }

  @Override
  public BitVector toBitVector(byte[] bytes) {
    if(bytes.length != numBytes) {
      throw new IllegalArgumentException("Wrong number of bytes in array");
    }

    long[] la = new long[bytes.length/8];

    for (int i=0; i< la.length; i++) {
      la[i] = (la[i] << 8) + (bytes[i+0] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+1] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+2] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+3] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+4] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+5] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+6] & 0xff);
      la[i] = (la[i] << 8) + (bytes[i+7] & 0xff);
    }

    return LongArrayBitVector.wrap(la);
  }

  @Override
  public long numBits() {
    return numBytes * 8;
  }

  @Override
  public long length(byte[] bytes) {
    return numBytes * 8;
  }
}
