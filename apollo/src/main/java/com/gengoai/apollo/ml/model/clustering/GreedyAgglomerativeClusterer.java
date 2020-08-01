/*
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

package com.gengoai.apollo.ml.model.clustering;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.collection.Iterables;
import com.gengoai.math.Optimum;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * A greedy implementation of agglomerative clustering where the clustering is not guaranteed to be optimal, but will
 * cluster significantly faster than typical agglomerative clustering.
 * </p>
 *
 * @author David B. Bracewell
 */
public class GreedyAgglomerativeClusterer extends HierarchicalClusterer {

   /**
    * Instantiates a new GreedyAgglomerativeClusterer with default parameters.
    */
   public GreedyAgglomerativeClusterer() {
      super(new ClusterFitParameters());
   }

   /**
    * Instantiates a new GreedyAgglomerativeClusterer with the given parameters.
    *
    * @param parameters the parameters
    */
   public GreedyAgglomerativeClusterer(@NonNull ClusterFitParameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new AgglomerativeClusterer with the given parameter updater.
    *
    * @param updater the updater
    */
   public GreedyAgglomerativeClusterer(@NonNull Consumer<ClusterFitParameters> updater) {
      super(with(new ClusterFitParameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      clustering = new HierarchicalClustering();
      clustering.setMeasure(parameters.measure.value());

      final Set<Cluster> active = dataset.parallelStream()
                                         .map(this::getNDArray)
                                         .zipWithIndex()
                                         .map((n, i) -> {
                                            Cluster c = new Cluster();
                                            c.setId(i.intValue());
                                            c.setCentroid(n);
                                            c.setScore(0);
                                            c.addPoint(n);
                                            return c;
                                         })
                                         .collect(Collectors.toSet());

      if (active.size() == 1) {
         clustering.root = Iterables.getFirst(active, null);
         return;
      }

      Cluster A = Iterables.getFirst(active, null);
      Cluster B = findOptimal(A, active);

      final Measure measure = parameters.measure.value();
      while (active.size() > 1) {
         double ab = measure.calculate(A.getCentroid(), B.getCentroid());

         Cluster C = findOptimal(B, active);
         double bc = measure.calculate(B.getCentroid(), C.getCentroid());

         if (A.equals(C) || measure.getOptimum().test(ab, bc)) {
            active.remove(A);
            active.remove(B);
            NDArray centroid = A.getCentroid().add(B.getCentroid()).divi(2.0);
            Cluster D = new Cluster();
            D.setId(A.getId());
            D.setCentroid(centroid);
            D.setScore(ab);
            D.setLeft(A);
            D.setRight(B);
            for (NDArray point : A.getPoints()) {
               D.addPoint(point);
            }
            for (NDArray point : B.getPoints()) {
               D.addPoint(point);
            }
            active.add(D);
            A = D;
            B = findOptimal(A, active);
         } else {
            A = B;
            B = C;
         }

      }

      clustering.root = Iterables.getFirst(active, null);

      int id = 0;
      Queue<Cluster> queue = new LinkedList<>();
      queue.add(clustering.root);
      while (queue.size() > 0) {
         Cluster c = queue.remove();
         if (c != null) {
            c.setId(id);
            id++;
            queue.add(c.getLeft());
            queue.add(c.getRight());
         }
      }

   }

   private Cluster findOptimal(Cluster a, Set<Cluster> active) {
      final Measure measure = parameters.measure.value();
      Stream<Tuple2<Cluster, Double>> stream = active.parallelStream()
                                                     .filter(c -> c != a)
                                                     .map(c -> $(c,
                                                                 measure.calculate(a.getCentroid(), c.getCentroid())));

      Optional<Tuple2<Cluster, Double>> optimal;
      if (measure.getOptimum() == Optimum.MAXIMUM) {
         optimal = stream.max(java.util.Map.Entry.comparingByValue());
      } else {
         optimal = stream.min(java.util.Map.Entry.comparingByValue());
      }

      return optimal.map(Tuple2::getV1).orElse(active.parallelStream()
                                                     .filter(c -> c != a)
                                                     .findFirst()
                                                     .orElse(null));
   }
}//END OF GreedyAgglomerativeClusterer
