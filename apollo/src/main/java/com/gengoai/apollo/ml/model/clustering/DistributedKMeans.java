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

import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.statistics.measure.Distance;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;

import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Distributed version of KMeans using Apache Spark.</p>
 *
 * @author David B. Bracewell
 */
public class DistributedKMeans extends FlatCentroidClusterer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new DistributedKMeans with default parameters.
    */
   public DistributedKMeans() {
      super(new Parameters());
   }

   /**
    * Instantiates a new DistributedKMeans with the given parameters.
    *
    * @param parameters the parameters
    */
   public DistributedKMeans(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new DistributedKMeans with the given parameter updater.
    *
    * @param updater the updater
    */
   public DistributedKMeans(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters fitParameters = Cast.as(parameters);
      org.apache.spark.mllib.clustering.KMeans kMeans = new org.apache.spark.mllib.clustering.KMeans();
      kMeans.setK(fitParameters.K.value());
      kMeans.setMaxIterations(fitParameters.maxIterations.value());
      kMeans.setEpsilon(fitParameters.tolerance.value());
      KMeansModel model = kMeans.run(dataset.stream()
                                            .toDistributedStream()
                                            .getRDD()
                                            .map(this::toDenseVector)
                                            .cache()
                                            .rdd());

      clustering = new FlatClustering();
      clustering.setMeasure(Distance.Euclidean);

      for(int i = 0; i < model.clusterCenters().length; i++) {
         Cluster cluster = new Cluster();
         cluster.setId(i);
         cluster.setCentroid(NDArrayFactory.ND.rowVector(model.clusterCenters()[i].toArray()));
         clustering.add(cluster);
      }
      dataset.stream()
             .map(n -> $(model.predict(toDenseVector(n)), n.get(parameters.input.value()).asNDArray()))
             .forEachLocal(t -> clustering.get(t.v1).addPoint(t.v2));

      for(Cluster cluster : clustering) {
         cluster.setScore(cluster.getScore() / cluster.size());
      }
   }

   @Override
   public Parameters getFitParameters() {
      return new Parameters();
   }

   private Vector toDenseVector(Datum o) {
      return (Vector) new DenseVector(o.get(parameters.input.value())
                                       .asNDArray()
                                       .toDoubleArray());
   }

   /**
    * Fit Parameters for KMeans
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

   }
}//END OF DistributedKMeans
