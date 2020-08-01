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

import com.gengoai.Validation;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.graph.Graph;

/**
 * @author David B. Bracewell
 */
public class DegreeScorer<V> extends AbstractVertexScorer<V> {

   private static final long serialVersionUID = -4636314189887346775L;

   /**
    * The degree to calculate
    */
   public enum DEGREE_TYPE {
      IN, OUT, TOTAL
   }

   private final DEGREE_TYPE rankType;

   /**
    * Default Constructor
    *
    * @param rankType Which degree to use for ranking
    */
   public DegreeScorer(DEGREE_TYPE rankType) {
      this.rankType = Validation.notNull(rankType);
   }

   @Override
   public Counter<V> score(Graph<V> g) {
      Validation.notNull(g, "The graph must not be null.");
      Counter<V> scores = Counters.newCounter();
      for (V vertex : g.vertices()) {
         switch (rankType) {
            case IN:
               scores.increment(vertex, g.inDegree(vertex));
               break;
            case OUT:
               scores.increment(vertex, g.outDegree(vertex));
               break;
            case TOTAL:
               scores.increment(vertex, g.degree(vertex));
               break;
         }
      }
      return scores;
   }

}//END OF DegreeScorer
