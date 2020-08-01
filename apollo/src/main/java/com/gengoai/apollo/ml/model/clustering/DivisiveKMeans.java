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

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.InMemoryDataSet;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

public class DivisiveKMeans extends FlatCentroidClusterer {
   private static final long serialVersionUID = 1L;

   public DivisiveKMeans() {
      super(new Parameters());
   }

   public DivisiveKMeans(@NonNull Consumer<Parameters> consumer) {
      super(with(new Parameters(), consumer));
   }


   private FlatClustering cluster(DataSet dataset) {
      KMeans kMeans = new KMeans(p -> {
         p.K.set(getFitParameters().K.value());
         p.input.set(getFitParameters().input.value());
         p.maxIterations.set(20);
         p.output.set(getFitParameters().output.value());
         p.measure.set(getFitParameters().measure.value());
         p.verbose.set(false);
      });
      kMeans.estimate(dataset);
      return Cast.as(kMeans.getClustering());
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      clustering = new FlatClustering();
      clustering.setMeasure(getFitParameters().measure.value());

      Queue<FlatClustering> queue = new LinkedList<>();
      queue.add(cluster(dataset));
      while (!queue.isEmpty()) {
         FlatClustering fc = queue.remove();
         for (Cluster c : fc) {
            if (c.size() == 0) {
               continue;
            }
            if (c.getScore() <= getFitParameters().tolerance.value() ||
                  c.getPoints().size() <= getFitParameters().minPoints.value()) {
               clustering.add(c);
            } else {
               DataSet ds = new InMemoryDataSet(c.getPoints().stream()
                                                 .map(n -> Datum.of($(getFitParameters().input.value(), n)))
                                                 .collect(Collectors.toList()));
               queue.add(cluster(ds));
            }
         }
      }

   }

   @Override
   public Parameters getFitParameters() {
      return Cast.as(parameters);
   }

   /**
    * Fit Parameters for DivisiveKMeans
    */
   public static class Parameters extends ClusterFitParameters {
      /**
       * The minimum number of points to form a cluster (default 4).
       */
      public final Parameter<Integer> minPoints = parameter(Params.Clustering.minPoints, 4);
      /**
       * The number of clusters to find for each call to KMeans (default 2).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 2);
      /**
       * The tolerance for including a cluster based on its score, i.e. if the score <= tolerance it is added and
       * otherwise will be further clustered (default 100d).
       */
      public final Parameter<Double> tolerance = parameter(Params.Optimizable.tolerance, 100d);
   }
}//END OF DivisiveKMeans
