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
 * <p>Common interface for writing graphs from resources.</p>
 * <p>The vertices of the graph are first converted into an intermediate representation ({@link
 * com.gengoai.graph.Vertex}), which defines a label and set of properties. Edges are translated into a property
 * map.</p>
 *
 * @param <V> the vertex type
 */
public interface GraphWriter<V> {


   /**
    * Sets the vertex encoder
    *
    * @param encoder the encoder
    */
   void setVertexEncoder(VertexEncoder<V> encoder);

   /**
    * Sets the edge encoder.
    *
    * @param encoder the encoder
    */
   void setEdgeEncoder(EdgeEncoder<V> encoder);


   /**
    * Writes the given graph to the give location in this format, using the defined vertex and edge encoders.
    *
    * @param graph    the graph to write
    * @param location the location to write to
    * @throws IOException Something went wrong writing the graph
    */
   default void write(Graph<V> graph, Resource location) throws IOException {
      write(graph, location, new ArrayListMultimap<>());
   }


   /**
    * Writes the given graph to the give location in this format, using the defined vertex and edge encoders.
    *
    * @param graph      the graph to write
    * @param location   the location to write to
    * @param parameters multimap of parameters used by the underling writer
    * @throws IOException Something went wrong writing the graph
    */
   void write(Graph<V> graph, Resource location, Multimap<String, String> parameters) throws IOException;

}//END OF GraphWriter
