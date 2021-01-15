package com.gengoai.graph.clustering;

import com.gengoai.graph.Graph;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.gengoai.collection.Sets.hashSetOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class StronglyConnectedComponentsTest {

   @Test
   public void cluster() {
      StronglyConnectedComponents<Integer> cc = new StronglyConnectedComponents<>();
      Graph<Integer> g = Graph.directed();
      IntStream.range(0, 8).forEach(g::addVertex);
      g.addEdge(0, 1);
      g.addEdge(1, 2);
      g.addEdge(2, 3);
      g.addEdge(3, 2);
      g.addEdge(3, 7);
      g.addEdge(7, 3);
      g.addEdge(2, 6);
      g.addEdge(7, 6);
      g.addEdge(5, 6);
      g.addEdge(6, 5);
      g.addEdge(1, 5);
      g.addEdge(4, 5);
      g.addEdge(4, 0);
      g.addEdge(1, 4);

      List<Set<Integer>> components = cc.cluster(g);

      assertEquals(3, components.size());

      Set<Integer> c1 = components.get(0);
      Set<Integer> c2 = components.get(1);
      Set<Integer> c3 = components.get(2);

      Set<Integer> e1 = hashSetOf(5, 6);
      Set<Integer> e2 = hashSetOf(2, 3, 7);
      Set<Integer> e3 = hashSetOf(0, 1, 4);

      assertTrue(c1.equals(e1) || c1.equals(e2) || c1.equals(e3));
      assertTrue(c2.equals(e1) || c2.equals(e2) || c2.equals(e3));
      assertTrue(c3.equals(e1) || c3.equals(e2) || c3.equals(e3));
   }
}