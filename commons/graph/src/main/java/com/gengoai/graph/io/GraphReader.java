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

import com.gengoai.graph.Graph;
import com.gengoai.io.resource.Resource;

import java.io.IOException;

/**
 * <p>Common interface for reading graphs from resources.</p>
 * <p>Readers first read in the graph constructing intermediate {@link com.gengoai.graph.Vertex} objects to represent
 * vertices and their properties. {@link com.gengoai.graph.Vertex} objects are then decoded into an actual vertex using
 * a supplied {@link VertexDecoder}. Similarly, edges are constructed using the graph's edge factory, but an {@link
 * EdgeDecoder} is used to add additional properties stored in a map to the edge.</p>
 *
 * @param <V> the vertex type
 * @author David B. Bracewell
 */
public interface GraphReader<V> {

   /**
    * Sets the vertex decoder.
    *
    * @param vertexDecoder the vertex decoder
    */
   void setVertexDecoder(VertexDecoder<V> vertexDecoder);

   /**
    * Sets the edge decoder.
    *
    * @param edgeDecoder the edge decoder
    */
   void setEdgeDecoder(EdgeDecoder<V> edgeDecoder);

   /**
    * Reads a graph in from a resource
    *
    * @param location the location of the graph
    * @return A Graph constructed based on the resource
    * @throws IOException Something went wrong reading
    */
   Graph<V> read(Resource location) throws IOException;

}//END OF GraphReader
