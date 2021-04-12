package com.gengoai.graph.search;

import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.graph.algorithms.BreadthFirstSearch;
import com.gengoai.graph.algorithms.DepthFirstSearch;
import org.junit.Test;

import java.util.List;

import static com.gengoai.collection.Lists.arrayListOf;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class SearchTest {


   @Test
   public void test() {
      Graph<String> g = Graph.directed();
      g.addVertices(arrayListOf("A", "B", "C", "D", "E"));
      g.addEdge("A", "C");
      g.addEdge("C", "E");
      g.addEdge("E", "D");
      g.addEdge("B", "D");
      g.addEdge("A", "B");

      BreadthFirstSearch<String> bfs = new BreadthFirstSearch<>(g);
      List<Edge<String>> path = bfs.search("A", "D");


      assertEquals(2, path.size());
      assertEquals("A", path.get(0).getFirstVertex());
      assertEquals("B", path.get(0).getSecondVertex());
      assertEquals("B", path.get(1).getFirstVertex());
      assertEquals("D", path.get(1).getSecondVertex());

      DepthFirstSearch<String> dfs = new DepthFirstSearch<>(g);
      path = dfs.search("A", "D");
      assertEquals(3, path.size());
      assertEquals("A", path.get(0).getFirstVertex());
      assertEquals("C", path.get(0).getSecondVertex());
      assertEquals("C", path.get(1).getFirstVertex());
      assertEquals("E", path.get(1).getSecondVertex());
      assertEquals("E", path.get(2).getFirstVertex());
      assertEquals("D", path.get(2).getSecondVertex());
   }
}