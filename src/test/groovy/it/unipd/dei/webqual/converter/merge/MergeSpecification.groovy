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
