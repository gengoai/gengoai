package com.gengoai.graph.algorithms;

import com.gengoai.graph.Graph;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class DepthFirstTraversalTest {


   @Test
   public void test() {
      Graph<Integer> g = Graph.directed();
      g.addVertices(Arrays.asList(1, 2, 3));
      g.addEdge(1, 2);
      g.addEdge(2, 3);

      DepthFirstTraversal<Integer> dft = new DepthFirstTraversal<>(g);

      Iterator<Integer> itr = dft.iterator(1);
      assertTrue(itr.hasNext());
      assertEquals(1, itr.next(), 0);
      assertTrue(itr.hasNext());
      assertEquals(2, itr.next(), 0);
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next(), 0);
   }
}