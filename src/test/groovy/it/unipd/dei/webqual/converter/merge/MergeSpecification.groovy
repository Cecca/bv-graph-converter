package it.unipd.dei.webqual.converter.merge

import spock.lang.Specification

class MergeSpecification extends Specification {

  class IntComp implements Comparator<Integer> {
    @Override
    int compare(Integer t1, Integer t2) {
      return t1.compareTo(t2)
    }
  }

  class IntMerger implements Merger<Integer> {
    @Override
    Integer merge(Integer first, Integer second) {
      return first
    }
  }

  class ArrMerger implements Merger<byte[]> {
    @Override
    byte[] merge(byte[] first, byte[] second) {
      return first
    }
  }

  def "Merged iterators are sorted" () {
    setup:
    def a = [1,2,3,4,5,6,7,8]
    def b = [3,4,5,6,7,8,9,10,11]
    def lit = new LazyMergeIterator(a.iterator(), b.iterator(), new IntComp(), new IntMerger())

    when:
    def merged = []
    while(lit.hasNext()) {
      merged.add(lit.next())
    }

    then:
    merged == [1,2,3,4,5,6,7,8,9,10,11]
  }

  def "Merged iterators contain no duplicates" () {
    setup:
    def a = [1,2,3,4,5,6,7,8]
    def b = [3,4,5,6,7,8,9,10,11]
    def lit = new LazyMergeIterator(a.iterator(), b.iterator(), new IntComp(), new IntMerger())

    when:
    def merged = []
    while(lit.hasNext()) {
      merged.add(lit.next())
    }

    then:
    merged == [1,2,3,4,5,6,7,8,9,10,11]
  }

  def "Merged iterators contain no duplicates with byte arrays" () {
    setup:
    def rnd = new Random()
    def first = []
    for(int i=0; i<10; i++) {
      byte[] b = new byte[16]
      rnd.nextBytes(b)
      first.add(b)
    }
    def second = []
    for(int i=0; i<10; i++) {
      byte[] b = new byte[16]
      rnd.nextBytes(b)
      second.add(b)
    }
    def lit = new LazyMergeIterator<byte[]>(first.iterator(), second.iterator(), new ArrayComparator(), new ArrMerger())

    when:
    def merged = []
    while(lit.hasNext()) {
      merged.add(lit.next())
    }

    then:
    containsNoDuplicates(merged)
  }

  def static containsNoDuplicates(merged) {
    ArrayComparator cmp = new ArrayComparator();
    Iterator<byte[]> it = merged.iterator();
    byte[] last = it.next();
    while(it.hasNext()) {
      byte[] cur = it.next();
      if(cmp.compare(cur, last) == 0) {
        return false
      }
      last = cur;
    }
    return true
  }

  def "Composed iterators yield the correct result an odd number of iterators" () {
    setup:
    def a = [1,2]
    def b = [1,3]
    def c = [4,6]
    def d = [3,5]
    def e = [7,8]

    def lit1 = new LazyMergeIterator(a.iterator(), b.iterator(), new IntComp(), new IntMerger())
    def lit2 = new LazyMergeIterator(c.iterator(), d.iterator(), new IntComp(), new IntMerger())
    def lit3 = new LazyMergeIterator(e.iterator(), new IntComp(), new IntMerger())

    def comp = LazyMergeIterator.compose(new IntComp(), new IntMerger(), lit1, lit2, lit3)

    when:
    def merged = []
    while(comp.hasNext()) {
      merged.add(comp.next())
    }

    then:
    merged == [1,2,3,4,5,6,7,8]
  }

  def "Composed iterators yield the correct result with an even number of iterators" () {
    setup:
    def a = [1,2]
    def b = [1,3]
    def c = [4,6]
    def d = [3,5]

    def lit1 = new LazyMergeIterator(a.iterator(), b.iterator(), new IntComp(), new IntMerger())
    def lit2 = new LazyMergeIterator(c.iterator(), d.iterator(), new IntComp(), new IntMerger())

    def comp = LazyMergeIterator.compose(new IntComp(), new IntMerger(), lit1, lit2)

    when:
    def merged = []
    while(comp.hasNext()) {
      merged.add(comp.next())
    }

    then:
    merged == [1,2,3,4,5,6]
  }

}
