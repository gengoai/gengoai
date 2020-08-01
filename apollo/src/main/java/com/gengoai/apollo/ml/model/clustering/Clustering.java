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

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.stream.Streams;
import lombok.NonNull;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * <p>
 * A clustering represents the result of a {@link Clusterer}. A clustering can be flat ({@link FlatClustering}) or
 * hierarchical ({@link HierarchicalClustering}).
 * </p>
 *
 * @author David B. Bracewell
 */
public interface Clustering extends Iterable<Cluster>, Serializable {
   /**
    * Calculates the total between-group variance of the clustering.
    *
    * @return the between-group variance
    */
   default double betweenGroupVariance() {
      Variance variance = new Variance();
      for(int i = 0; i < size(); i++) {
         Cluster c = get(i);
         for(int j = 0; j < size(); j++) {
            if(i != j) {
               variance.increment(getMeasure().calculate(c.getCentroid(), get(i).getCentroid()));
            }
         }
      }
      return variance.getResult();
   }

   /**
    * Gets the cluster for the given index.
    *
    * @param index the index
    * @return the cluster
    */
   Cluster get(int index);

   /**
    * Gets the measure used to compute the distance/similarity between points.
    *
    * @return the measure
    */
   Measure getMeasure();

   /**
    * Gets the root of the hierarchical cluster.
    *
    * @return the root
    */
   Cluster getRoot();

   /**
    * Calculates the total in-group variance of the clustering.
    *
    * @return the in-group variance
    */
   default double inGroupVariance() {
      Variance variance = new Variance();
      for(int i = 0; i < size(); i++) {
         Cluster c = get(i);
         for(NDArray point : c.getPoints()) {
            variance.increment(getMeasure().calculate(point, c.getCentroid()));
         }
      }
      return variance.getResult();
   }

   /**
    * Checks if the clustering is flat
    *
    * @return True if flat, False otherwise
    */
   boolean isFlat();

   /**
    * Checks if the clustering is hierarchical
    *
    * @return True if hierarchical, False otherwise
    */
   boolean isHierarchical();

   /**
    * Keeps only the centroid vectors removing all other points.
    */
   default void keepOnlyCentroids() {
      forEach(Cluster::clear);
   }

   default int selectBestCluster(@NonNull Observation observation) {
      throw new UnsupportedOperationException();
   }

   /**
    * Sets the measure used to compute the distance/similarity between points.
    *
    * @param measure the measure
    */
   void setMeasure(@NonNull Measure measure);

   /**
    * The number of clusters
    *
    * @return the number of clusters
    */
   int size();

   /**
    * Creates a stream of the clusters
    *
    * @return the stream of clusters
    */
   default Stream<Cluster> stream() {
      return Streams.asStream(this);
   }

}//END OF Clustering
