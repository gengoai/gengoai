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


import com.gengoai.graph.Edge;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>Interface decoding a set of properties and applying to a given edge.</p>
 *
 * @param <V> the type of the vertex
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface EdgeDecoder<V> extends Serializable {

   /**
    * Decodes the properties in the <code>Map</code> adding them to the edge as necessary.
    *
    * @param edge       the edge
    * @param properties the properties
    * @return the edge with the decoded properties
    */
   Edge<V> decode(Edge<V> edge, Map<String, String> properties);

}//END OF EdgeDecoder
