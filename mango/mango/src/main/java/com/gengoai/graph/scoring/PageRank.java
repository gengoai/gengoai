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
public class PageRank<V> extends AbstractVertexScorer<V> {

   private static final long serialVersionUID = -7329282877612595474L;
   private final double dampingFactor;
   private final int maxIterations;
   private final double tolerance;

   public PageRank() {
      this(100, 0.85, 0.001);
   }

   public PageRank(int maxIterations, double dampingFactor, double tolerance) {
      this.maxIterations = maxIterations;
      this.dampingFactor = dampingFactor;
      this.tolerance = tolerance;
   }

   @Override
   public Counter<V> score(Graph<V> g) {
      Validation.notNull(g, "The graph must not be null.");
      Counter<V> pageRank = Counters.newCounter();
      for (V vertex : g.vertices()) {
         pageRank.increment(vertex, 1d / g.numberOfVertices());
      }

      double lastValue = 0d;
      for (int itr = 0; itr < maxIterations; itr++) {
         double thisValue = 0d;

         Counter<V> thisRound = Counters.newCounter();
         for (V vertex : g.vertices()) {

            double pr = 0;

            for (V v2 : g.getPredecessors(vertex)) {
               pr += (pageRank.get(v2) / g.outDegree(v2));
            }

            pr = (1 - dampingFactor) + dampingFactor * pr;
            thisValue += pr;
            thisRound.set(vertex, pr);
         }

         if (Math.abs(thisValue - lastValue) < tolerance) {
            break;
         }
         lastValue = thisValue;
         pageRank = thisRound;
      }
      return pageRank;
   }

}//END OF PageRank
