package com.gengoai.graph.algorithms;

import com.gengoai.graph.Graph;

import java.util.Deque;

/**
 * <p>Traverses the graph in a depth first fashion.</p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public class DepthFirstTraversal<V> extends AbstractGraphTraversal<V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Depth first visitor.
    *
    * @param graph the graph to visit
    */
   public DepthFirstTraversal(Graph<V> graph) {
      super(graph);
   }

   @Override
   protected void add(Deque<V> deque, V vertex) {
      deque.push(vertex);
   }

   @Override
   protected V nextV(Deque<V> deque) {
      return deque.pop();
   }

}//END OF BreadthFirstVisitor
