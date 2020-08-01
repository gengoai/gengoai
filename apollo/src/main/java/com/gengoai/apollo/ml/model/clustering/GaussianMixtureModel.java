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
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximization;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;

/**
 * <p>Gaussian Mixture Model</p>
 *
 * @author David B. Bracewell
 */
public class GaussianMixtureModel extends FlatCentroidClusterer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new GaussianMixtureModel with default parameters.
    */
   public GaussianMixtureModel() {
      super(new Parameters());
   }

   /**
    * Instantiates a new GaussianMixtureModel with the given parameters.
    *
    * @param parameters the parameters
    */
   public GaussianMixtureModel(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new GaussianMixtureModel with the given parameter updater.
    *
    * @param updater the updater
    */
   public GaussianMixtureModel(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters p = Cast.as(parameters);
      clustering = new FlatClustering();
      clustering.setMeasure(p.measure.value());

      final List<NDArray> vectors = dataset.parallelStream()
                                           .map(this::getNDArray)
                                           .collect();
      int numberOfFeatures = (int) vectors.get(0).length();
      int numberOfDataPoints = (int) vectors.size();
      double[][] data = new double[numberOfDataPoints][numberOfFeatures];
      int i = 0;
      for (NDArray vector : vectors) {
         data[i] = vector.toDoubleArray();
         i++;
      }
      List<MultivariateNormalDistribution> components =
            MultivariateNormalMixtureExpectationMaximization.estimate(data, p.K.value())
                                                            .getComponents()
                                                            .stream()
                                                            .map(Pair::getSecond)
                                                            .collect(Collectors.toList());
      for (i = 0; i < components.size(); i++) {
         Cluster cluster = new Cluster();
         cluster.setId(i);
         cluster.setCentroid(NDArrayFactory.ND.columnVector(components.get(i).sample()));
         clustering.add(cluster);
      }
   }

   @Override
   public Parameters getFitParameters() {
      return new Parameters();
   }

   /**
    * Gaussian Mixture Model Fit Parameters
    */
   public static class Parameters extends ClusterFitParameters {
      private static final long serialVersionUID = 1L;
      /**
       * The number of clusters (default 100).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 100);
   }

}//END OF GaussianMixtureModel
