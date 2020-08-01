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
import com.gengoai.collection.LRUMap;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;
import com.gengoai.tuple.Tuple2;

import java.util.*;

/**
 * <p>Implementation of <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm</a> for
 * finding the shortest paths between vertices in a graph. </p>
 *
 * @param <V> the Vertex type parameter
 * @author David B. Bracewell
 */
public class DijkstraShortestPath<V> implements SingleSourceShortestPath<V>, ShortestPath<V>, GraphSearch<V> {

   private final Graph<V> graph;
   private final Map<V, ListMultimap<V, Edge<V>>> pathMap = new LRUMap<>(100);
   private final boolean treatUndirected;

   /**
    * Instantiates a new Dijkstra shortest path.
    *
    * @param graph the graph to calculate paths over
    */
   public DijkstraShortestPath(Graph<V> graph) {
      this(graph, false);
   }

   /**
    * Instantiates a new Dijkstra shortest path.
    *
    * @param graph           the graph to calculate paths over
    * @param treatUndirected True treat the graph as undirected (even if it is directed).
    */
   public DijkstraShortestPath(Graph<V> graph, boolean treatUndirected) {
      this.graph = graph;
      this.treatUndirected = treatUndirected;
   }

   @Override
   public List<Edge<V>> search(V startingPoint, V endingPoint) {
      return path(startingPoint, endingPoint);
   }

   @Override
   public Counter<V> singleSourceShortestDistance(V source) {
      Validation.checkArgument(graph.containsVertex(source), "Vertex must be in the graph.");

      Counter<V> distances = Counters.newCounter();

      if (!pathMap.containsKey(source)) {
         singleSourceShortestPath(source);
      }

      for (V v : graph.vertices()) {
         if (!v.equals(source)) {
            if (pathMap.get(source).containsKey(v)) {
               double distance = 0d;
               for (Edge<V> e : pathMap.get(source).get(v)) {
                  distance += e.getWeight();
               }
               distances.set(v, distance);
            } else {
               distances.set(v, Double.POSITIVE_INFINITY);
            }
         }
      }


      return distances;
   }

   @Override
   public ListMultimap<V, Edge<V>> singleSourceShortestPath(V source) {
      Validation.checkArgument(graph.containsVertex(source), "Vertex must be in the graph.");

      if (pathMap.containsKey(source)) {
         return pathMap.get(source);
      }

      Counter<V> distances = Counters.newCounter();
      Set<V> visited = new HashSet<>(Collections.singleton(source));
      Map<V, V> previous = new HashMap<>();

      distances.set(source, 0d);
      for (V v : graph.vertices()) {
         if (!v.equals(source)) {
            distances.set(v, Double.POSITIVE_INFINITY);
         }
      }

      PriorityQueue<Tuple2<V, Double>> queue = new PriorityQueue<>(Comparator.comparing(Tuple2::getV2));
      queue.add(Tuple2.of(source, 0d));

      while (!queue.isEmpty()) {
         Tuple2<V, Double> next = queue.remove();
         V u = next.v1;
         visited.add(u);

         for (Edge<V> out : treatUndirected ? graph.getEdges(u) : graph.getOutEdges(u)) {
            V v = out.getOppositeVertex(u);
            double alt = distances.get(u) + out.getWeight();
            if (alt < distances.get(v)) {
               distances.set(v, alt);
               previous.put(v, u);
               if (!visited.contains(v)) {
                  queue.add(Tuple2.of(v, alt));
               }
            }
         }

      }

      ListMultimap<V, Edge<V>> list = new ArrayListMultimap<>();
      pathMap.put(source, list);
      for (V v : graph.vertices()) {
         if (v.equals(source)) {
            continue;
         }
         if (Double.isFinite(distances.get(v))) {
            Deque<V> stack = new LinkedList<>();
            V u = v;
            while (u != null && previous.containsKey(u)) {
               stack.push(u);
               u = previous.get(u);
            }
            V from = source;
            while (!stack.isEmpty()) {
               V to = stack.pop();
               Edge<V> edge = graph.getEdge(from, to);
               if (treatUndirected && edge == null) {
                  edge = graph.getEdge(to, from);
               }
               list.put(v, edge);
               from = to;
            }
         }
      }
      return list;
   }

   @Override
   public double distance(V source, V target) {
      Validation.checkArgument(graph.containsVertex(source), "Vertex must be in the graph.");
      Validation.checkArgument(graph.containsVertex(target), "Vertex must be in the graph.");
      return singleSourceShortestDistance(source).get(target);
   }

   @Override
   public List<Edge<V>> path(V source, V target) {
      Validation.checkArgument(graph.containsVertex(source), "Vertex must be in the graph.");
      Validation.checkArgument(graph.containsVertex(target), "Vertex must be in the graph.");
      return Collections.unmodifiableList(singleSourceShortestPath(source).get(target));
   }

   /**
    * Resets cached data
    */
   public void reset() {
      pathMap.clear();
   }


}//END OF DijkstraShortestPath
