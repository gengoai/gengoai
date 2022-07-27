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

package com.gengoai.graph.clustering;

import com.gengoai.Validation;
import com.gengoai.graph.Graph;

import java.io.Serializable;
import java.util.*;

/**
 * Implementation of <a href="https://en.wikipedia.org/wiki/Strongly_connected_component"> Strongly Connected
 * components</a>.
 *
 * @param <V> the vertex type
 */
public class StronglyConnectedComponents<V> implements Clusterer<V>, Serializable {
   private static final long serialVersionUID = 1L;

   private int index;
   private Stack<V> vertexStack;
   private Set<V> visited;
   private Map<V, Integer> vertexLowLink;
   private Graph<V> graph;
   private List<Set<V>> clusters;

   @Override
   public List<Set<V>> cluster(Graph<V> g) {
      Validation.notNull(g);
      graph = g;
      index = 0;
      vertexStack = new Stack<>();
      visited = new HashSet<>();
      vertexLowLink = new HashMap<>();
      clusters = new ArrayList<>();
      for (V v : g.vertices()) {
         if (!visited.contains(v)) {
            strongConnect(v);
         }
      }

      vertexLowLink = null;
      graph = null;
      vertexStack = null;

      return clusters;
   }

   private void strongConnect(V vertex) {
      visited.add(vertex);
      vertexLowLink.put(vertex, index);
      vertexStack.push(vertex);
      int min = index;

      index++;

      for (V w : graph.getSuccessors(vertex)) {
         if (!visited.contains(w)) {
            strongConnect(w);
         }
         min = Math.min(min, vertexLowLink.get(w));
      }

      if (min < vertexLowLink.get(vertex)) {
         vertexLowLink.put(vertex, min);
         return;
      }

      Set<V> cluster = new HashSet<>();
      V w;
      do {
         w = vertexStack.pop();
         cluster.add(w);
      } while (!w.equals(vertex) && vertexStack.size() > 0);

      if (!cluster.isEmpty()) {
         clusters.add(cluster);
      }
   }


}//END OF ConnectedComponents
