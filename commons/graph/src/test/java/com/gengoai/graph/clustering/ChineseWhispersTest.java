package com.gengoai.graph.clustering;

import com.gengoai.graph.Graph;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class ChineseWhispersTest {

   @Test
   public void cluster() {
      ChineseWhispers<Integer> cc = new ChineseWhispers<>(20, 0.9, 0.05);
      Graph<Integer> g = Graph.directed();
      IntStream.range(0, 5).forEach(g::addVertex);
      g.addEdge(0, 1, 10);
      g.addEdge(0, 2, 5);
      g.addEdge(3, 2, 5);
      g.addEdge(3, 4, 20);

      List<Set<Integer>> components = cc.cluster(g);
      assertEquals(3, components.size(), 2);
   }
}