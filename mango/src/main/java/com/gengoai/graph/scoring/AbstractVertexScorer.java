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

package com.gengoai.graph.scoring;

import com.gengoai.collection.Maps;
import com.gengoai.graph.Graph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.gengoai.Validation.notNull;

/**
 * Abstract base vertex scorer that provides the rank method
 *
 * @author David B. Bracewell
 */
public abstract class AbstractVertexScorer<V> implements VertexScorer<V>, Serializable {

   private static final long serialVersionUID = -2758053889213808095L;

   @Override
   public List<Map.Entry<V, Double>> rank(Graph<V> g) {
      return Maps.sortEntriesByValue(score(notNull(g)).asMap(), false);
   }


}//END OF AbstractVertexScorer
