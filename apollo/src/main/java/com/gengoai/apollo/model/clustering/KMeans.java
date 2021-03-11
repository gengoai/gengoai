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

package com.gengoai.apollo.model.clustering;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.compose.VectorCompositions;
import com.gengoai.apollo.math.measure.Measure;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.model.Params;
import com.gengoai.apollo.model.StoppingCriteria;
import com.gengoai.concurrent.AtomicDouble;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Optimum;
import com.gengoai.stream.MStream;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;

/**
 * <p>
 * Implementation of <a href="https://en.wikipedia.org/wiki/K-means_clustering">K-means</a> Clustering using Loyd's
 * algorithm.
 * </p>
 *
 * @author David B. Bracewell
 */
@Log
public class KMeans extends FlatCentroidClusterer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new KMeans with default parameters.
    */
   public KMeans() {
      super(new Parameters());
   }

   /**
    * Instantiates a new KMeans with the given parameters.
    *
    * @param parameters the parameters
    */
   public KMeans(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new KMeans with the given parameter updater.
    *
    * @param updater the updater
    */
   public KMeans(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   private Cluster estimate(NDArray n) {
      n = transform(n).asNDArray();
      if (parameters.measure.value().getOptimum() == Optimum.MAXIMUM) {
         return clustering.get((int) n.shape().calculateOffset(n.argMax()));
      }
      return clustering.get((int) n.shape().calculateOffset(n.argMin()));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters fitParameters = Cast.as(parameters);
      clustering = new FlatClustering();
      clustering.setMeasure(fitParameters.measure.value());
      MStream<NumericNDArray> vectorStream = dataset.parallelStream().map(this::getNDArray);
      for (NumericNDArray centroid : initCentroids(fitParameters.K.value(), vectorStream)) {
         Cluster c = new Cluster();
         c.setCentroid(centroid);
         clustering.add(c);
      }
      final Measure measure = fitParameters.measure.value();
      StoppingCriteria.create("numPointsChanged")
                      .historySize(3)
                      .maxIterations(fitParameters.maxIterations.value())
                      .tolerance(fitParameters.tolerance.value())
                      .reportInterval(fitParameters.verbose.value() ? 1 : -1)
                      .logger(log)
                      .untilTermination(itr -> this.iteration(vectorStream));

      for (int i = 0; i < clustering.size(); i++) {
         Cluster cluster = clustering.get(i);
         cluster.setId(i);
         if (cluster.size() > 0) {
            cluster.getPoints().removeIf(Objects::isNull);
            double average = cluster.getPoints()
                                    .parallelStream()
                                    .flatMapToDouble(p1 -> cluster.getPoints()
                                                                  .stream()
                                                                  .filter(p2 -> p2 != p1)
                                                                  .mapToDouble(p2 -> measure.calculate(p1, p2)))
                                    .summaryStatistics()
                                    .getAverage();
            cluster.setScore(average);
         } else {
            cluster.setScore(Double.MAX_VALUE);
         }
      }
   }

   @Override
   public Parameters getFitParameters() {
      return Cast.as(parameters);
   }


   private NumericNDArray[] initCentroids(int K, MStream<NumericNDArray> instances) {
      return instances.sample(false, K)
                      .map(NumericNDArray::copy)
                      .collect(Collectors.toList())
                      .toArray(new NumericNDArray[0]);
   }

   private double iteration(MStream<NumericNDArray> instances) {
      clustering.keepOnlyCentroids();

      final Object[] locks = new Object[clustering.size()];
      for (int i = 0; i < clustering.size(); i++) {
         locks[i] = new Object();
      }

      //Assign points
      AtomicDouble numChanged = new AtomicDouble();
      instances.parallel().forEach(v -> {
         Cluster c = estimate(v);
         synchronized (locks[c.getId()]) {
            c.addPoint(v);
         }
         if (v.getPredicted() == null) {
            v.setPredicted((double) c.getId());
         } else {
            numChanged.addAndGet(v.<Double>getPredicted().intValue() == c.getId() ? 0 : 1);
         }
      });

      for (Cluster c : clustering) {
         if( c.getPoints().isEmpty() ){
            c.setCentroid(VectorCompositions.Average.compose(Arrays.asList(initCentroids(10, instances))));
         } else {
            c.setCentroid(VectorCompositions.Average.compose(c.getPoints()));
         }
      }

      return numChanged.get();
   }

   /**
    * Fit Parameters for KMeans
    */
   public static class Parameters extends ClusterFitParameters {
      /**
       * The number of clusters (default 2).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 2);
      /**
       * The maximum number of iterations to run the clusterer for (default 100).
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 100);
      /**
       * The tolerance in change of in-group variance for determining if k-means has converged (default 1e-3).
       */
      public final Parameter<Double> tolerance = parameter(Params.Optimizable.tolerance, 1e-3);
   }
}//END OF KMeans
