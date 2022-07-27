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

package com.gengoai.graph.clustering;

import com.gengoai.Validation;
import com.gengoai.collection.Lists;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.graph.Edge;
import com.gengoai.graph.Graph;

import java.util.*;

/**
 * <p>
 * Implementation of the Chinese Whispers graph clustering algorithm as outlined in
 * <ul>
 * <li>Biemann, Chris. "Chinese whispers: an efficient graph clustering algorithm and its application to natural
 * language processing problems." Proceedings of the First Workshop on Graph Based Methods for Natural Language
 * Processing. Association for Computational Linguistics, 2006.</li>
 * </ul>
 * </p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public class ChineseWhispers<V> implements Clusterer<V> {

   private int maxIterations;
   private double acceptanceRate;
   private double newClusterRate;

   /**
    * Default constructor <code>maxIterations = 10</code>, <code>acceptanceRate = 1</code>, and <code>newClusterRate =
    * 0d</code>
    */
   public ChineseWhispers() {
      this(10, 1d, 0d);
   }

   /**
    * Initialization constructor
    *
    * @param maxIterations  The maximum number of iterations to run the clustering
    * @param acceptanceRate The rate used to determine if a label change should be accepted
    * @param newClusterRate the new cluster rate
    */
   public ChineseWhispers(int maxIterations, double acceptanceRate, double newClusterRate) {
      Validation.checkArgument(maxIterations > 0);
      Validation.checkArgument(acceptanceRate > 0 && acceptanceRate <= 1);
      this.maxIterations = maxIterations;
      this.acceptanceRate = acceptanceRate;
      this.newClusterRate = newClusterRate;
   }

   /**
    * Gets new cluster rate.
    *
    * @return the new cluster rate
    */
   public double getNewClusterRate() {
      return newClusterRate;
   }

   /**
    * Sets new cluster rate.
    *
    * @param newClusterRate the new cluster rate
    */
   public void setNewClusterRate(double newClusterRate) {
      this.newClusterRate = newClusterRate;
   }

   @Override
   public List<Set<V>> cluster(Graph<V> g) {

      // Assign initial clusters
      Map<V, Integer> classMap = new HashMap<>();
      int numClasses = g.numberOfVertices();
      for (V vertex : g.vertices()) {
         classMap.put(vertex, classMap.size());
      }

      List<V> shuffled = Lists.asArrayList(g.vertices());

      for (int itr = 0; itr < maxIterations; itr++) {
         boolean converged = true;

         // shuffle the vertices
         Collections.shuffle(shuffled);
         Random rnd = new Random();

         for (V v1 : shuffled) {

            // Determine the max label
            Counter<Integer> labelCntr = Counters.newCounter();
            double maxScore = Double.NEGATIVE_INFINITY;
            int maxV = -1;
            for (V v2 : g.getSuccessors(v1)) {
               Integer cOfV2 = classMap.get(v2);
               Edge<V> edge = g.getEdge(v1, v2);
               labelCntr.increment(cOfV2, edge.isWeighted() ? edge.getWeight() : 1.0d);
               if (labelCntr.get(cOfV2) > maxScore) {
                  maxScore = labelCntr.get(cOfV2);
                  maxV = cOfV2;
               }
            }


            double p = rnd.nextDouble();


            if (p < (newClusterRate * (maxIterations - itr) / maxIterations)) {
               classMap.put(v1, numClasses);
               numClasses++;
               converged = false;
            } else if (maxV != -1 && p < acceptanceRate) {
               if (maxV != classMap.put(v1, maxV)) {
                  converged = false;
               }
            }


         }

         if (converged) {
            break;
         }
      }

      // Construct the cluster list
      List<Set<V>> rval = new ArrayList<>();
      for (Integer id : Sets.asHashSet(classMap.values())) {
         Set<V> cluster = new HashSet<>();
         for (Map.Entry<V, Integer> entry : classMap.entrySet()) {
            if (entry.getValue().equals(id)) {
               cluster.add(entry.getKey());
            }
         }
//         for (V v : classMap.keySet()) {
//            if (classMap.get(v).equals(id)) {
//               cluster.add(v);
//            }
//         }
         rval.add(cluster);
      }

      return rval;
   }

   /**
    * Gets max iterations.
    *
    * @return the maxIterations
    */
   public int getMaxIterations() {
      return maxIterations;
   }

   /**
    * Sets max iterations.
    *
    * @param maxIterations the maxIterations to set
    */
   public void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }

   /**
    * Gets acceptance rate.
    *
    * @return the acceptanceRate
    */
   public double getAcceptanceRate() {
      return acceptanceRate;
   }

   /**
    * Sets acceptance rate.
    *
    * @param acceptanceRate the acceptanceRate to set
    */
   public void setAcceptanceRate(double acceptanceRate) {
      this.acceptanceRate = acceptanceRate;
   }

}// END OF ChineseWhispers
