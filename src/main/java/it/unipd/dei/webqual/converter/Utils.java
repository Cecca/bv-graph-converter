package it.unipd.dei.webqual.converter;

public class Utils {

  /** `long` mask with the first bit set */
  public static final long HEAD_MASK_L = 1L << 63;

  /** `long` mask with all the bits other than the most significant set. */
  public static final long RESET_MASK_L = ~HEAD_MASK_L;

  /** `byte` mask with the most significant bit set. */
  public static final byte HEAD_MASK = (byte) (1 << 7);

  public static final byte RESET_MASK = (byte) ~HEAD_MASK;

  /**
   * Creates a `long` from the first 8 bytes of the 16 bytes ID.
   */
  public static long getLong(byte[] id128) {

    long l = 0;
    // Loop manually unrolled
    // for (int i = 0; i < 8; i++)
    // {
    //   l = (l << 8) + (id128[i] & 0xff);
    // }
    l = (l << 8) + (id128[0] & 0xff);
    l = (l << 8) + (id128[1] & 0xff);
    l = (l << 8) + (id128[2] & 0xff);
    l = (l << 8) + (id128[3] & 0xff);
    l = (l << 8) + (id128[4] & 0xff);
    l = (l << 8) + (id128[5] & 0xff);
    l = (l << 8) + (id128[6] & 0xff);
    l = (l << 8) + (id128[7] & 0xff);
    return l;
  }

  public static boolean isHead(byte[] id) {
    return (id[0] & HEAD_MASK) == HEAD_MASK;
  }

  public static boolean isHead(long id) {
    return (id & HEAD_MASK_L) == HEAD_MASK_L;
  }

  public static byte[] setHead(byte[] id) {
    id[0] = (byte) (id[0] | HEAD_MASK);
    return id;
  }

  public static long setHead(long id) {
    return id | HEAD_MASK_L;
  }

  public static long reset(long id) {
    return id & RESET_MASK_L;
  }

  public static byte[] reset(byte[] id) {
    id[0] = (byte) (id[0] & RESET_MASK);
    byte[] res = new byte[id.length];
    System.arraycopy(id, 0, res, 0, id.length);
    return res;
  }

}
