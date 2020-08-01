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
import com.gengoai.apollo.math.statistics.Sampling;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.StoppingCriteria;
import com.gengoai.conversion.Cast;
import com.gengoai.tuple.Tuple2;
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Implementation of KMeans using MiniBatch for better scalability.</p>
 *
 * @author David B. Bracewell
 */
@Log
public class MiniBatchKMeans extends FlatCentroidClusterer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new MiniBatchKMeans with default parameters.
    */
   public MiniBatchKMeans() {
      super(new Parameters());
   }

   /**
    * Instantiates a new MiniBatchKMeans with the given parameters.
    *
    * @param parameters the parameters
    */
   public MiniBatchKMeans(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new MiniBatchKMeans with the given parameter updater.
    *
    * @param updater the updater
    */
   public MiniBatchKMeans(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   private Tuple2<Integer, Double> best(NDArray v) {
      int bestId = 0;
      final Measure measure = parameters.measure.value();
      double bestMeasure = measure.calculate(v, clustering.get(0).getCentroid());
      for (int j = 1; j < clustering.size(); j++) {
         double score = measure.calculate(v, clustering.get(j).getCentroid());
         if (parameters.measure.value().getOptimum().test(score, bestMeasure)) {
            bestId = j;
            bestMeasure = score;
         }
      }
      return $(bestId, bestMeasure);
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters parameters = getFitParameters();
      clustering = new FlatClustering();
      clustering.setMeasure(parameters.measure.value());
      final List<NDArray> vectors = dataset.parallelStream()
                                           .map(this::getNDArray)
                                           .collect();
      PrimitiveIterator.OfInt itr = Sampling.uniformInts(parameters.K.value(), 0, (int) vectors.size(), false)
                                            .iterator();

      for (int i = 0; i < parameters.K.value(); i++) {
         Cluster c = new Cluster();
         c.setId(i);
         c.setCentroid(vectors.get(itr.nextInt()).copy());
         clustering.add(c);
      }

      final int[] counts = new int[parameters.K.value()];
      StoppingCriteria.create("avg_distance", parameters)
                      .logger(log)
                      .untilTermination(iteration -> iteration(vectors, counts, parameters.batchSize.value()));

      //Assign examples to clusters
      final Integer[] locks = IntStream.range(0, parameters.K.value()).boxed().toArray(Integer[]::new);
      vectors.parallelStream()
             .forEach(v -> {
                Tuple2<Integer, Double> best = best(v);
                final Cluster c = clustering.get(best.v1);
                synchronized (locks[c.getId()]) {
                   c.addPoint(v);
                }
             });
   }

   @Override
   public Parameters getFitParameters() {
      return Cast.as(parameters);
   }

   private double iteration(List<NDArray> stream, int[] counts, int batchSize) {
      //Select batch and compute the best cluster for each item
      List<Tuple3<NDArray, Integer, Double>> batch =
            Sampling.uniformInts(batchSize, 0, (int) stream.size(), true)
                    .parallel()
                    .mapToObj(i -> best(stream.get(i)).appendLeft(stream.get(i)))
                    .collect(Collectors.toList());

      //Update the centroids based on the assignments
      double diff = 0d;
      for (Tuple3<NDArray, Integer, Double> assignment : batch) {
         NDArray target = assignment.v1;
         int cid = assignment.v2;
         counts[cid]++;
         double eta = 1.0 / counts[cid];
         NDArray centroid = clustering.get(cid).getCentroid();
         centroid.muli(1.0 - eta).addi(target.mul(eta));
         diff += assignment.v3;
      }
      diff /= batch.size();
      return diff;
   }

   /**
    * Fit Parameters for MiniBatchKMeans
    */
   public static class Parameters extends ClusterFitParameters {
      private static final long serialVersionUID = 1L;
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
      /**
       * The size of the batch for processing kmeans (default 1000)
       */
      public final Parameter<Integer> batchSize = parameter(Params.Optimizable.batchSize, 1000);
      /**
       * The amount of history to record to determine convergence (default 10)
       */
      public final Parameter<Integer> historySize = parameter(Params.Optimizable.historySize, 10);
      /**
       * The iteration interval to report statistics on the clustering (default 10).
       */
      public final Parameter<Integer> reportInterval = parameter(Params.Optimizable.reportInterval, 10);

   }
}//END OF MiniBatchKMeans
