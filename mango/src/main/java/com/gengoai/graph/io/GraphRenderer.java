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

package com.gengoai.graph.io;


import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.graph.Graph;
import com.gengoai.io.resource.Resource;

import java.io.IOException;

/**
 * <p>Common interface for rendering a graph</p>
 *
 * @param <V> the vertex type
 */
public interface GraphRenderer<V> {

   /**
    * Sets the vertex encoder
    *
    * @param vertexEncoder the encoder
    */
   void setVertexEncoder(VertexEncoder<V> vertexEncoder);

   /**
    * Sets the edge encoder.
    *
    * @param edgeEncoder the encoder
    */
   void setEdgeEncoder(EdgeEncoder<V> edgeEncoder);

   /**
    * Renders the given graph to the give location, using the defined vertex and edge encoders.
    *
    * @param graph    the graph to render
    * @param location the location to write the rendering to
    * @throws IOException Something went wrong rendering the graph
    */
   default void render(Graph<V> graph, Resource location) throws IOException {
      render(graph, location, new ArrayListMultimap<>());
   }


   /**
    * Renders the given graph to the give location, using the defined vertex and edge encoders.
    *
    * @param graph      the graph to render
    * @param location   the location to write the rendering to
    * @param parameters multimap of parameters used by the underling render
    * @throws IOException Something went wrong rendering the graph
    */
   void render(Graph<V> graph, Resource location, Multimap<String, String> parameters) throws IOException;

}//END OF GraphRenderer
