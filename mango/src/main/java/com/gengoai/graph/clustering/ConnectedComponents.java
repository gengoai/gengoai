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
import com.gengoai.graph.algorithms.BreadthFirstTraversal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of <a href="https://en.wikipedia.org/wiki/Connected_component_(graph_theory)">Connected
 * components</a>.
 *
 * @param <V> the vertex type
 */
public class ConnectedComponents<V> implements Clusterer<V>, Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public List<Set<V>> cluster(Graph<V> g) {
      Validation.notNull(g);
      List<Set<V>> rval = new ArrayList<>();
      Set<V> seen = new HashSet<>();
      BreadthFirstTraversal<V> visitor = new BreadthFirstTraversal<>(g);
      for (V v : g.vertices()) {
         if (seen.contains(v)) continue;
         Set<V> cluster = new HashSet<>();
         visitor.iterator(v).forEachRemaining(cluster::add);
         rval.add(cluster);
         seen.addAll(cluster);
      }

      return rval;
   }

}//END OF ConnectedComponents
