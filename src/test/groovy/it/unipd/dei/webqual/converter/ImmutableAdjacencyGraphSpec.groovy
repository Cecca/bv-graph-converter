package it.unipd.dei.webqual.converter

import it.unimi.dsi.big.webgraph.BVGraph
import it.unimi.dsi.big.webgraph.EFGraph
import it.unimi.dsi.fastutil.longs.LongBigArrays
import spock.lang.Specification

import static it.unipd.dei.webqual.converter.Utils.*

class ImmutableAdjacencyGraphSpec extends Specification {

  def writeGraph(graph, filename) {
    def out = new DataOutputStream(new FileOutputStream(filename))
    for (l in graph) {
      def head = l.head()
      def neighs = l.tail()
      out.writeLong(head | HEAD_MASK_L)
      for (n in neighs) {
        out.writeLong(n)
      }
    }
    out.close()
  }

  def "test conversion" () {
    setup:
    def filename = "/tmp/immutable-graph-test"
    // create graph
    def graph = [ [0L, 1L, 2L, 3L]
                , [1L, 0L]
                , [2L, 0L, 3L]
                , [3L, 0L, 2L]]
    writeGraph(graph, filename)
    def ig = ImmutableAdjacencyGraph.loadOffline(filename, 8, null)
    BVGraph.store(ig, filename+"-bv")
    def bvg = BVGraph.load(filename+"-bv")

    def nit = ig.nodeIterator()
    while(nit.hasNext()) {
      print(nit.nextLong())
      println("  -  "+LongBigArrays.toString(nit.successorBigArray()))
    }

    expect:
    bvg.numNodes() == 4
    bvg.numArcs() == 8
    bvg.outdegree(0) == 3

  }

  def "test conversion: shuffled nodes" () {
    setup:
    def filename = File.createTempFile("immutable-graph-test", "").getCanonicalPath()
    // create graph
    def graph = [ [0L, 1L, 2L, 3L]
            , [2L, 0L, 3L]
            , [1L, 0L]
            , [3L, 0L, 2L]]
    writeGraph(graph, filename)
    def ig = ImmutableAdjacencyGraph.loadOffline(filename, 8, null)
    EFGraph.store(ig, filename+"-ef")
    def efg = EFGraph.load(filename+"-ef")
    BVGraph.store(efg,filename+"-bv")
    def bvg = BVGraph.load(filename+"-bv")

    def nit = ig.nodeIterator()
    while(nit.hasNext()) {
      print(nit.nextLong())
      println("  -  "+LongBigArrays.toString(nit.successorBigArray()))
    }

    expect:
    efg.numNodes() == 4
    efg.numArcs() == 8
    efg.outdegree(0) == 3

    bvg.numNodes() == 4
    bvg.numArcs() == 8
    bvg.outdegree(0) == 3

  }

  def "test conversion: shuffled nodes non adjacent" () {
    setup:
    def filename = File.createTempFile("immutable-graph-test", "").getCanonicalPath()
    // create graph
    def graph = [ [0L, 2L, 3L]
                , [3L, 0L, 2L]
                , [2L, 0L, 3L]]
    writeGraph(graph, filename)
    def ig = ImmutableAdjacencyGraph.loadOffline(filename, 8, null)
    EFGraph.store(ig, filename+"-ef")
    def efg = EFGraph.load(filename+"-ef")
    BVGraph.store(efg,filename+"-bv")
    def bvg = BVGraph.load(filename+"-bv")

    def nit = ig.nodeIterator()
    while(nit.hasNext()) {
      print(nit.nextLong())
      println("  -  "+LongBigArrays.toString(nit.successorBigArray()))
    }

    expect:
    efg.numNodes() == 3
    efg.numArcs() == 6
    efg.outdegree(0) == 2

    bvg.numNodes() == 3
    bvg.numArcs() == 6
    bvg.outdegree(0) == 2

  }

}
