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

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.graph.Edge;

/**
 * A shortest path interface that calculates the path for a single vertex to all other vertices in the graph.
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public interface SingleSourceShortestPath<V> {


   /**
    * The distance between the source vertex and all other vertices in the graph
    *
    * @param source the source vertex
    * @return Counter of target vertices and their distances.
    */
   Counter<V> singleSourceShortestDistance(V source);

   /**
    * The path (list of edges) between the source vertex and all other vertices in the graph
    *
    * @param source the source vertex
    * @return ListMultimap of target vertices and paths from source to target.
    */
   ListMultimap<V, Edge<V>> singleSourceShortestPath(V source);


}//END OF SingleSourceShortestPath
