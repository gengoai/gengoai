package com.gengoai.graph.algorithms;

import com.gengoai.graph.Graph;

import java.util.Deque;

/**
 * <p>Traverses the graph in a breadth first fashion.</p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public class BreadthFirstTraversal<V> extends AbstractGraphTraversal<V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Breadth first visitor.
    *
    * @param graph the graph to visit
    */
   public BreadthFirstTraversal(Graph<V> graph) {
      super(graph);
   }

   @Override
   protected void add(Deque<V> deque, V vertex) {
      deque.offer(vertex);
   }

   @Override
   protected V nextV(Deque<V> deque) {
      return deque.remove();
   }
}//END OF BreadthFirstVisitor
