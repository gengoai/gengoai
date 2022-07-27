package com.gengoai.graph.clustering;

import com.gengoai.graph.Graph;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.gengoai.collection.Sets.hashSetOf;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class ConnectedComponentsTest {

   @Test
   public void cluster() {
      ConnectedComponents<String> cc = new ConnectedComponents<>();
      Graph<String> g = Graph.undirected();
      g.addVertices(Arrays.asList("A", "B", "C", "D", "E", "F", "G"));
      //Connected Component 1
      g.addEdge("A", "B");
      g.addEdge("A", "D");
      g.addEdge("B", "C");
      g.addEdge("C", "D");
      //Connected Component 2
      g.addEdge("E", "F");
      g.addEdge("E", "G");


      List<Set<String>> components = cc.cluster(g);

      assertEquals(2, components.size());

      Set<String> c1 = components.get(0).contains("A") ? components.get(0)
                                                       : components.get(1);

      Set<String> c2 = components.get(0).contains("A") ? components.get(1)
                                                       : components.get(0);


      assertEquals(hashSetOf("A", "B", "C", "D"), c1);
      assertEquals(hashSetOf("E", "F", "G"), c2);

   }
}