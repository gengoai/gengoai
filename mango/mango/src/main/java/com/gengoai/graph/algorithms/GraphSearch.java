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
 * <p>Searches for a path between a given starting point and given ending point.</p>
 *
 * @param <V> the vertex type parameter
 * @author David B. Bracewell
 */
public interface GraphSearch<V> {

   /**
    * Searches for a path from the given starting point to the given ending point
    *
    * @param startingPoint the starting point
    * @param endingPoint   the ending point
    * @return the path as a list of edges (empty list if no path exists)
    */
   List<Edge<V>> search(V startingPoint, V endingPoint);

}//END OF GraphSearch
