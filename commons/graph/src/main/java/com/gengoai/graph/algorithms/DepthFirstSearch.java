package com.gengoai.graph.algorithms;

import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.tuple.Tuple2;

import java.util.Deque;
import java.util.List;

import static com.gengoai.Validation.notNull;

/**
 * <p>Performs a depth first search to find a path between the given starting point and ending point.</p>
 *
 * @param <V> the vertex type parameter
 * @author David B. Bracewell
 */
public class DepthFirstSearch<V> extends AbstractGraphSearch<V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new DepthFirstSearch.
    *
    * @param graph the graph to search
    */
   public DepthFirstSearch(Graph<V> graph) {
      super(notNull(graph));
   }

   @Override
   protected void add(Deque<List<Tuple2<V, Edge<V>>>> pathes, List<Tuple2<V, Edge<V>>> path) {
      pathes.push(path);
   }

   @Override
   protected List<Tuple2<V, Edge<V>>> next(Deque<List<Tuple2<V, Edge<V>>>> pathes) {
      return pathes.pop();
   }
}//END OF BreadthFirstSearch
