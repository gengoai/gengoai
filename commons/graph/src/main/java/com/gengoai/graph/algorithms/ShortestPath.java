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


import com.gengoai.graph.Edge;

import java.util.List;

/**
 * Calculates the shortest path between two vertices.
 *
 * @param <V> the vertex type parameter
 * @author David B. Bracewell
 */
public interface ShortestPath<V> {

   /**
    * The distance between the two vertices.
    *
    * @param source the starting (source) vertex
    * @param target the target vertex
    * @return the distance from source to target (Positive Infinity if no path exists).
    */
   double distance(V source, V target);

   /**
    * The shortest path (list of edges) from the source to target vertex.
    *
    * @param source the starting (source) vertex
    * @param target the target vertex
    * @return List of edges from source to target representing the shortest path.
    */
   List<Edge<V>> path(V source, V target);

}//END OF ShortestPath
