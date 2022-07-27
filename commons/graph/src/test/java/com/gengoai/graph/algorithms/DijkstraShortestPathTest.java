package com.gengoai.graph.algorithms;

import com.gengoai.graph.Graph;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class DijkstraShortestPathTest {


   @Test
   public void test() {
      Graph<String> g = Graph.directed();
      g.addVertices(Arrays.asList("A", "B", "C"));
      g.addEdge("A", "B", 20);
      g.addEdge("A", "C", 2);
      g.addEdge("C", "B", 10);

      DijkstraShortestPath<String> shortestPath = new DijkstraShortestPath<>(g);

      assertEquals(12, shortestPath.distance("A", "B"), 0);




   }
}