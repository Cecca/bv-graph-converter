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

  def "test isHead for buffers" () {
    expect:
    def b = buf.collect{i -> (byte) i}.toArray(new byte[buf.size()])
    isHead(b) == expected

    where:
    buf                 || expected
    [0,0,0,0]           || false
    [0xff,0,0,0]        || true
  }

}
