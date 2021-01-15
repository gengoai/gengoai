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
 * <p>Encodes an edge into a series of properties. For example, an edge weight would be encoded with a key of
 * <code>weight</code> with a string representation of the value.</p>
 *
 * @param <V> the vertex type
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface EdgeEncoder<V> extends Serializable {


  /**
   * Encodes the properties of an endge into map.
   *
   * @param edge the edge
   * @return the properties
   */
  Map<String, String> encode(Edge<V> edge);

}//END OF EdgeEncoder
