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

import com.gengoai.conversion.Converter;
import com.gengoai.graph.Vertex;

import java.util.Collections;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Default encoders and decoders.</p>
 *
 * @author David B. Bracewell
 */
public interface DefaultEncodersDecoders {
   /**
    * Key used to store edge weight
    */
   String EDGE_WEIGHT_KEY = "weight";

   /**
    * Default vertex encoder which converts the sets the encoded label as the string version of the given vertex.
    *
    * @param <V> the vertex type
    * @return the vertex encoder
    */
   static <V> VertexEncoder<V> defaultVertexEncoder() {
      return vertex -> Vertex.builder().label(Converter.convertSilently(vertex, String.class)).build();
   }

   /**
    * Default vertex decoder which converts the label into to the vertex type.
    *
    * @param <V>         the vertex type
    * @param vertexClass the vertex class
    * @return the vertex decoder
    */
   static <V> VertexDecoder<V> defaultVertexDecoder(final Class<V> vertexClass) {
      return vertex -> Converter.convertSilently(vertex.getLabel(), vertexClass);
   }

   /**
    * Default edge encoder which encodes the edge weight if the graph is weighted.
    *
    * @param <V> the vertex type
    * @return the edge encoder
    */
   static <V> EdgeEncoder<V> defaultEdgeEncoder() {
      return edge -> {
         if (edge.isWeighted()) {
            return hashMapOf($(EDGE_WEIGHT_KEY, Double.toString(edge.getWeight())));
         } else {
            return Collections.emptyMap();
         }
      };
   }

   /**
    * Default edge decoder which is aware of edge weights.
    *
    * @param <V> the vertex type
    * @return the edge decoder
    */
   static <V> EdgeDecoder<V> defaultEdgeDecoder() {
      return (edge, properties) -> {
         if (properties != null && properties.containsKey(EDGE_WEIGHT_KEY)) {
            edge.setWeight(Double.parseDouble(properties.get(EDGE_WEIGHT_KEY)));
         }
         return edge;
      };
   }



}//END OF DefaultEncodersDecoders
