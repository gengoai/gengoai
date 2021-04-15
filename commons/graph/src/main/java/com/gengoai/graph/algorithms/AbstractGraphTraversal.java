package com.gengoai.graph.algorithms;

import com.gengoai.graph.Graph;

import java.io.Serializable;
import java.util.*;


/**
 * Abstract traversal for depth first and breadth first traversals
 *
 * @param <V> the type parameter
 */
public abstract class AbstractGraphTraversal<V> implements GraphTraversal<V>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Graph<V> graph;

   /**
    * Instantiates a new Abstract graph traversal.
    *
    * @param graph the graph
    */
   public AbstractGraphTraversal(Graph<V> graph) {
      this.graph = graph;
   }

   protected Graph<V> getGraph() {
      return graph;
   }

   @Override
   public final Iterator<V> iterator(V startingPoint) {
      return new GenericIterator(startingPoint);
   }

   /**
    * Add.
    *
    * @param deque  the deque
    * @param vertex the vertex
    */
   protected abstract void add(Deque<V> deque, V vertex);

   /**
    * Next v v.
    *
    * @param deque the deque
    * @return the v
    */
   protected abstract V nextV(Deque<V> deque);

   private class GenericIterator implements Iterator<V> {
      private final Deque<V> deque = new LinkedList<>();
      private final Set<V> visited = new HashSet<>();

      /**
       * Instantiates a new Generic iterator.
       *
       * @param startingVertex the starting vertex
       */
      protected GenericIterator(V startingVertex) {
         this.deque.offer(startingVertex);
      }

      @Override
      public boolean hasNext() {
         return deque.size() > 0;
      }

      @Override
      public V next() {
         if (deque.isEmpty()) {
            throw new NoSuchElementException();
         }
         V top = nextV(deque);
         graph.getSuccessors(top).stream()
              .filter(v -> !visited.contains(v) && !deque.contains(v))
              .forEachOrdered(v -> add(deque, v));
         visited.add(top);
         return top;
      }
   }
}//END OF AbstractGraphVisitor
