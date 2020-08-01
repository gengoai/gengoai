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
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.SingleSourceModel;
import com.gengoai.string.Strings;
import lombok.NonNull;

/**
 * <p>
 * Clustering is an unsupervised machine learning algorithm that partitions input objects often based on the distance
 * between them in their feature space. Different clustering algorithms may require the number of partitions (clusters)
 * to specified as a parameter whereas others may determine the optimal number of clusters automatically.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class Clusterer extends SingleSourceModel<ClusterFitParameters, Clusterer> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Clusterer.
    *
    * @param parameters the parameters
    */
   protected Clusterer(@NonNull ClusterFitParameters parameters) {
      super(parameters);
   }

   /**
    * Gets clustering.
    *
    * @return the clustering
    */
   public abstract Clustering getClustering();

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(parameters.output.value())) {
         return LabelType.NDArray;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   protected NDArray getNDArray(Datum datum) {
      NDArray n = datum.get(parameters.input.value()).asNDArray();
      if (Strings.isNotNullOrBlank(parameters.output.value()) &&
            datum.containsKey(parameters.output.value())) {
         n.setLabel(datum.get(parameters.output.value()));
      }
      return n;
   }

   /**
    * Gets number of clusters.
    *
    * @return the number of clusters
    */
   public abstract int getNumberOfClusters();

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setDimension(getNumberOfClusters());
         m.setEncoder(null);
         m.setType(NDArray.class);
      });
   }

}//END OF Clusterer
