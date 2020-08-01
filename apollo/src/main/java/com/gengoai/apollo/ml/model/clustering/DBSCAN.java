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

import com.gengoai.ParameterDef;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import java.util.List;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;

/**
 * <p>
 * A wrapper around the DBSCAN clustering algorithm in Apache Math. DBSCAN is a flat centroid based clustering where the
 * number of clusters does not need to be specified.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DBSCAN extends FlatCentroidClusterer {
   public static final ParameterDef<Double> eps = ParameterDef.doubleParam("eps");
   public static final ParameterDef<Integer> minPts = ParameterDef.intParam("minPts");
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new DBSCAN model with default parameters.
    */
   public DBSCAN() {
      super(new Parameters());
   }

   /**
    * Instantiates a new DBSCAN model with the given parameters.
    *
    * @param parameters the parameters
    */
   public DBSCAN(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new DBSCAN model with the given parameter updater.
    *
    * @param updater the updater
    */
   public DBSCAN(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Parameters fitParameters = Cast.as(parameters);
      clustering = new FlatClustering();
      clustering.setMeasure(fitParameters.measure.value());
      DBSCANClusterer<ApacheClusterable> clusterer = new DBSCANClusterer<>(fitParameters.eps.value(),
                                                                           fitParameters.minPts.value(),
                                                                           new ApacheDistanceMeasure(
                                                                                 fitParameters.measure.value()));
      List<ApacheClusterable> apacheClusterables = dataset.parallelStream()
                                                          .map(d -> new ApacheClusterable(getNDArray(d)))
                                                          .collect();

      List<org.apache.commons.math3.ml.clustering.Cluster<ApacheClusterable>> result = clusterer.cluster(
            apacheClusterables);
      for (int i = 0; i < result.size(); i++) {
         Cluster cp = new Cluster();
         cp.setId(i);
         cp.setCentroid(result.get(i).getPoints().get(0).getVector());
         clustering.add(cp);
      }

      apacheClusterables.forEach(a -> {
         NDArray n = a.getVector();
         int index = -1;
         double score = fitParameters.measure.value().getOptimum().startingValue();
         for (int i = 0; i < clustering.size(); i++) {
            Cluster c = clustering.get(i);
            double s = fitParameters.measure.value().calculate(n, c.getCentroid());
            if (fitParameters.measure.value().getOptimum().test(s, score)) {
               index = i;
               score = s;
            }
         }
         clustering.get(index).addPoint(n);
      });
   }

   @Override
   public Parameters getFitParameters() {
      return new Parameters();
   }

   /**
    * FitParameters for DBSCAN
    */
   public static class Parameters extends ClusterFitParameters {
      private static final long serialVersionUID = 1L;
      /**
       * the maximum distance between two vectors to be in the same region
       */
      public final Parameter<Double> eps = parameter(DBSCAN.eps, 1.0);
      /**
       * the minimum number of points to form  a dense region
       */
      public final Parameter<Integer> minPts = parameter(DBSCAN.minPts, 2);
   }

}//END OF DBSCAN
