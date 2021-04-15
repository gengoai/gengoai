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

package com.gengoai.graph;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.json.JsonEntry;

import java.io.Serializable;

/**
 * Interface for creating new edges.
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface EdgeFactory<V> extends Serializable {

   /**
    * Creates an edge between the from and to vertices with the given weight
    *
    * @param from   the from
    * @param to     the to
    * @param weight the weight
    * @return the edge
    */
   Edge<V> createEdge(V from, V to, double weight);

   /**
    * Creates an edge between the from and to vertices filling in any given edge properties (e.g. weight) using the
    * {@link JsonEntry}. This method is used for deserializing a graph from json.
    *
    * @param from  the from
    * @param to    the to
    * @param entry the entry
    * @return the edge
    */
   Edge<V> createEdge(V from, V to, JsonEntry entry);

   /**
    * Gets the type of edge this factory generates (used to validate edges being added to the graph).
    *
    * @return the edge class
    */
   Class<? extends Edge> getEdgeClass();

   /**
    * Indicates whether the factor produces directed or undirected edges.
    *
    * @return True if the edges constructed in this factory are directed, False indicates undirected
    */
   boolean isDirected();

}//END OF EdgeFactory
