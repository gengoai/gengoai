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
 *
 */

package com.gengoai.apollo.ml.model.clustering;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * Implementation of Hierarchical Agglomerative Clustering
 * </p>
 *
 * @author David B. Bracewell
 */
public class AgglomerativeClusterer extends HierarchicalClusterer {
   private static final long serialVersionUID = 1L;
   private final AtomicInteger idGenerator = new AtomicInteger();

   /**
    * Instantiates a new AgglomerativeClusterer with default parameters.
    */
   public AgglomerativeClusterer() {
      super(new Parameters());
   }

   /**
    * Instantiates a new AgglomerativeClusterer with the given parameters.
    *
    * @param parameters the parameters
    */
   public AgglomerativeClusterer(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new AgglomerativeClusterer with the given parameter updater.
    *
    * @param updater the updater
    */
   public AgglomerativeClusterer(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   private void doTurn(PriorityQueue<Tuple3<Cluster, Cluster, Double>> priorityQueue,
                       List<Cluster> clusters,
                       Parameters parameters
                      ) {
      Tuple3<Cluster, Cluster, Double> minC = priorityQueue.remove();
      if(minC != null) {
         priorityQueue.removeIf(triple -> triple.v2.getId() == minC.v2.getId()
                                      || triple.v1.getId() == minC.v1.getId()
                                      || triple.v2.getId() == minC.v1.getId()
                                      || triple.v1.getId() == minC.v2.getId()
                               );
         Cluster cprime = new Cluster();
         cprime.setId(idGenerator.getAndIncrement());
         cprime.setLeft(minC.getV1());
         cprime.setRight(minC.getV2());
         minC.getV1().setParent(cprime);
         minC.getV2().setParent(cprime);
         cprime.setScore(minC.v3);
         for(NDArray point : Iterables.concat(minC.getV1().getPoints(), minC.getV2().getPoints())) {
            cprime.addPoint(point);
         }
         clusters.remove(minC.getV1());
         clusters.remove(minC.getV2());

         priorityQueue.addAll(clusters.parallelStream()
                                      .map(c2 -> $(cprime, c2,
                                                   parameters.linkage.value()
                                                                     .calculate(cprime, c2,
                                                                                parameters.measure.value())))
                                      .collect(Collectors.toList()));

         clusters.add(cprime);
      }

   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters parameters = Cast.as(getFitParameters());
      clustering = new HierarchicalClustering();
      clustering.setMeasure(parameters.measure.value());

      final List<NDArray> vectors = dataset.parallelStream()
                                           .map(datum -> datum.get(parameters.input.value()).asNDArray())
                                           .collect();

      idGenerator.set(0);
      PriorityQueue<Tuple3<Cluster, Cluster, Double>> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(Tuple3::getV3));
      List<Cluster> clusters = initDistanceMatrix(vectors, priorityQueue, parameters);
      while(clusters.size() > 1) {
         doTurn(priorityQueue, clusters, parameters);
      }
      clustering.root = clusters.get(0);
   }

   @Override
   public Parameters getFitParameters() {
      return new Parameters();
   }

   private List<Cluster> initDistanceMatrix(List<NDArray> instances,
                                            PriorityQueue<Tuple3<Cluster, Cluster, Double>> priorityQueue,
                                            Parameters parameters
                                           ) {
      List<Cluster> clusters = new ArrayList<>();
      for(NDArray item : instances) {
         Cluster c = new Cluster();
         c.addPoint(item);
         c.setId(idGenerator.getAndIncrement());
         clusters.add(c);
      }

      final int size = (int) instances.size();
      priorityQueue.addAll(IntStream.range(0, size - 2)
                                    .boxed()
                                    .flatMap(i -> IntStream.range(i + 1, size)
                                                           .boxed()
                                                           .map(j -> $(clusters.get(i), clusters.get(j))))
                                    .parallel()
                                    .map(
                                          t -> $(t.v1, t.v2,
                                                 parameters.linkage.value()
                                                                   .calculate(t.v1, t.v2, parameters.measure.value())))
                                    .collect(Collectors.toList()));
      return clusters;
   }

   /**
    * {@link FitParameters} for Agglomerative Clustering
    */
   public static class Parameters extends ClusterFitParameters {
      private static final long serialVersionUID = 1L;
      /**
       * The linkage to use for computing the distance between clusters
       */
      public final Parameter<Linkage> linkage = parameter(Params.Clustering.linkage, Linkage.Complete);
   }

}//END OF AgglomerativeClusterer
