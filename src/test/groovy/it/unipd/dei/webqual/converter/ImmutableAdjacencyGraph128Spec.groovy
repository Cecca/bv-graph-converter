package it.unipd.dei.webqual.converter

import spock.lang.Specification
import static it.unipd.dei.webqual.converter.ImmutableAdjacencyGraph128.*

class ImmutableAdjacencyGraph128Spec extends Specification {

  def "test getLong"() {
    setup:
    def rnd = new Random()
    def list = (1..16).collect{(byte) rnd.nextInt()}
    def buf = list.toArray(new byte[16])
    byte[] shortBuf = list.take(8).toArray(new byte[8])
    def bigInt = new BigInteger(shortBuf).longValue()
    def parsed = getLong(buf)

    expect:
    bigInt == parsed
  }

}
