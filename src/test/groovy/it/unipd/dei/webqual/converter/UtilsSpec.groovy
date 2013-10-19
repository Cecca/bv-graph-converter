package it.unipd.dei.webqual.converter

import spock.lang.Specification
import static it.unipd.dei.webqual.converter.Utils.*

class UtilsSpec extends Specification {

  def "Head manipulation secification" () {
    setup:
    def rnd = new Random()
    def arr = new byte[8]
    for(int i = 0; i<arr.length; i++) {
      arr[i] = (byte) rnd.nextInt()
    }

    expect:
    isHead(setHead(arr))
    arr == reset(setHead(arr))
    !isHead(reset(arr))
  }

}
