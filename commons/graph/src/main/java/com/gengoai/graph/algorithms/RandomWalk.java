/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.graph.algorithms;

import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.counter.Counter;
import com.gengoai.graph.Graph;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * <p>Performs a random walk over the vertices in the graph. If the graph is weighted, the next vertex is randomly
 * selected proportional to the successor vertices' weight. If the graph is not weighted, the next vertex is uniformly
 * randomly selected. </p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public class RandomWalk<V> implements GraphTraversal<V>, Serializable {

   private static final long serialVersionUID = -6167223778955711361L;
   private final boolean isWeighted;
   private final Graph<V> graph;

   /**
    * Default Constructor testPath(source);
    *
    * @param graph The graph we will visit
    */
   public RandomWalk(Graph<V> graph) {
      this(graph, false);
   }

   /**
    * Default Constructor
    *
    * @param graph      The graph we will visit
    * @param isWeighted True if use weights to determine the next step
    */
   public RandomWalk(Graph<V> graph, boolean isWeighted) {
      this.graph = Validation.notNull(graph);
      this.isWeighted = isWeighted;
   }

   @Override
   public Iterator<V> iterator(V startingVertex) {
      return new RandomWalkIterator<>(graph, Validation.notNull(startingVertex), isWeighted);
   }

   /**
    * Selecte a vertex by taking a given number of steps from the starting vertex
    *
    * @param startingVertex the starting vertex
    * @param numberOfSteps  the number of steps
    * @return the vertex ended upon after the given number of steps.
    */
   public V walk(V startingVertex, int numberOfSteps) {
      Validation.notNull(startingVertex);
      Validation.checkArgument(numberOfSteps >= 0, "Number of steps must be >= 0.");
      V next = startingVertex;
      int i = 0;
      for (Iterator<V> iterator = iterator(startingVertex); iterator.hasNext() && i < numberOfSteps; ) {
         next = iterator.next();
         i++;
      }
      return next;
   }

   private static class RandomWalkIterator<V> implements Iterator<V> {

      private final Random random = new Random();
      private final Graph<V> graph;
      private final boolean weighted;
      private V currentVertex;
      private V last = null;

      /**
       * Instantiates a new Random walk iterator.
       *
       * @param graph         the graph
       * @param currentVertex the current vertex
       * @param weighted      the weighted
       */
      public RandomWalkIterator(Graph<V> graph, V currentVertex, boolean weighted) {
         this.graph = graph;
         this.currentVertex = currentVertex;
         this.weighted = weighted;
      }

      private void getNext() {
         if (graph.outDegree(currentVertex) == 0) {
            currentVertex = null;
         } else if (!weighted) {
            currentVertex = Iterables.get(graph.getSuccessors(currentVertex),
                                          random.nextInt(graph.outDegree(currentVertex)), null);
         } else {
            Counter<V> weights = graph.getSuccessorWeights(currentVertex);
            weights.divideBySum();
            double randomValue = random.nextDouble();
            double sum;
            List<V> next = weights.itemsByCount(true);
            for (V key : next) {
               sum = weights.get(key);
               if (sum <= randomValue) {
                  currentVertex = key;
                  break;
               }
            }
         }
      }

      @Override
      public boolean hasNext() {
         return currentVertex != null;
      }

      @Override
      public V next() {
         if (currentVertex == null) {
            throw new NoSuchElementException();
         }
         last = currentVertex;
         getNext();
         return last;
      }

      @Override
      public void remove() {
         if (currentVertex == null) {
            throw new NoSuchElementException();
         }
         graph.removeVertex(currentVertex);
         currentVertex = last;
      }
   }

}//END OF RandomWalkVisitor
