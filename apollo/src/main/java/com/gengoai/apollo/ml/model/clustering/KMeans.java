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
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.StoppingCriteria;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Optimum;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

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
         return clustering.get((int) n.argmax());
      }
      return clustering.get((int) n.argmin());
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters fitParameters = Cast.as(parameters);
      clustering = new FlatClustering();
      clustering.setMeasure(fitParameters.measure.value());
      final List<NDArray> vectors = dataset.parallelStream()
                                           .map(this::getNDArray)
                                           .collect();

      for (NDArray centroid : initCentroids(fitParameters.K.value(), vectors)) {
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
                      .untilTermination(itr -> this.iteration(vectors));
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

   private NDArray[] initCentroids(int K, List<NDArray> instances) {
      final int dim = (int) instances.get(0).length();
      NDArray[] clusters = IntStream.range(0, K)
                                    .mapToObj(i -> NDArrayFactory.ND.array(dim))
                                    .toArray(NDArray[]::new);

      double[] cnts = new double[K];
      Random rnd = new Random();
      for (NDArray ii : instances) {
         int ci = rnd.nextInt(K);
         clusters[ci].addi(ii);
         cnts[ci]++;
      }
      for (int i = 0; i < K; i++) {
         if (cnts[i] > 0) {
            clusters[i].divi((float) cnts[i]);
         }
      }
      return clusters;
   }

   private double iteration(List<NDArray> instances) {
      final int dim = (int) instances.get(0).length();
      //Clear the points
      clustering.keepOnlyCentroids();

      final Object[] locks = new Object[clustering.size()];
      for (int i = 0; i < clustering.size(); i++) {
         locks[i] = new Object();
      }
      //Assign points
      instances.parallelStream().forEach(v -> {
         Cluster c = estimate(v);
         synchronized (locks[c.getId()]) {
            c.addPoint(v);
         }
      });

      double numChanged = 0;
      for (Cluster cluster : clustering) {
         NDArray centroid;

         //Calculate the new centroid, randomly generating a new vector when the custer has 0 members
         if (cluster.size() == 0) {
            centroid = NDArrayFactory.ND.uniform(Shape.shape(dim), -1, 1);
         } else {
            centroid = NDArrayFactory.ND.array(dim);
            for (NDArray point : cluster.getPoints()) {
               centroid.addi(point);
            }
            centroid.divi(cluster.size());
         }
         cluster.setCentroid(centroid);

         //Calculate the number of points tht changed from the previous iteration
         numChanged += cluster.getPoints()
                              .parallelStream()
                              .mapToDouble(n -> {
                                 if (n.getPredicted() == null) {
                                    n.setPredicted((double) cluster.getId());
                                    return 1.0;
                                 }
                                 double c = n.getPredictedAsDouble() == cluster.getId()
                                       ? 0
                                       : 1;
                                 n.setPredicted((double) cluster.getId());
                                 return c;
                              }).sum();
      }

      return numChanged;
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
