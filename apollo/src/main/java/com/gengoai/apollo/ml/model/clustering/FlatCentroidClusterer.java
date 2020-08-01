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
import com.gengoai.apollo.ml.observation.Observation;
import lombok.NonNull;

/**
 * <p>Abstract base class for {@link Clusterer}s whose resultant clustering is flat, i.e. are a set of K lists of
 * points where K is the number of clusters and are defined via centroids, i.e. central points.</p>
 *
 * @author David B. Bracewell
 */
public abstract class FlatCentroidClusterer extends Clusterer {
   protected FlatClustering clustering;

   protected FlatCentroidClusterer(@NonNull ClusterFitParameters parameters) {
      super(parameters);
   }

   @Override
   public Clustering getClustering() {
      return clustering;
   }

   @Override
   public int getNumberOfClusters() {
      return clustering == null
             ? 0
             : clustering.size();
   }

   @Override
   protected final Observation transform(@NonNull Observation observation) {
      NDArray distances = NDArrayFactory.DENSE.array(clustering.size());
      for(int i = 0; i < clustering.size(); i++) {
         distances.set(i, clustering.getMeasure().calculate(observation.asNDArray(), clustering.get(i).getCentroid()));
      }
      return distances;
   }
}//END OF FlatCentroidClustering
