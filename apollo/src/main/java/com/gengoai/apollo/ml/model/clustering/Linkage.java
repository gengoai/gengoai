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
import com.gengoai.apollo.math.statistics.measure.Measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Criteria for determining the distance between two clusters.
 *
 * @author David B. Bracewell
 */
public enum Linkage {
   /**
    * Single link, which calculates the minimum distance between elements
    */
   Single {
      @Override
      public double calculate(DoubleStream doubleStream) {
         return doubleStream.min().orElse(Double.POSITIVE_INFINITY);
      }
   },
   /**
    * Complete link, which calculates the maximum distance between elements
    */
   Complete {
      @Override
      public double calculate(DoubleStream doubleStream) {
         return doubleStream.max().orElse(Double.POSITIVE_INFINITY);
      }
   },
   /**
    * Average link, which calculates the mean distance between elements
    */
   Average {
      @Override
      public double calculate(DoubleStream doubleStream) {
         return doubleStream.average().orElse(Double.POSITIVE_INFINITY);
      }
   };

   /**
    * Calculates a value over the given stream of doubles which represents a stream of distances between an instance and
    * a cluster.
    *
    * @param doubleStream the double stream
    * @return the calculated value
    */
   public abstract double calculate(DoubleStream doubleStream);

   /**
    * Calculates the linkage metric between two clusters
    *
    * @param c1              the first cluster
    * @param c2              the second cluster
    * @param distanceMeasure the distance measure to use
    * @return the linkage metric
    */
   public final double calculate(Cluster c1, Cluster c2, Measure distanceMeasure) {
      List<Double> distances = new ArrayList<>();
      for (NDArray t1 : flatten(c1)) {
         distances.addAll(flatten(c2).stream()
                                     .map(t2 -> distanceMeasure.calculate(t1, t2))
                                     .collect(Collectors.toList()));
      }
      return calculate(distances.stream().mapToDouble(Double::doubleValue));
   }

   /**
    * Calculates the linkage metric between a vector and a cluster
    *
    * @param v               the vector
    * @param cluster         the cluster
    * @param distanceMeasure the distance measure to use
    * @return the linkage metric
    */
   public final double calculate(NDArray v, Cluster cluster, Measure distanceMeasure) {
      return calculate(cluster.getPoints().stream().mapToDouble(v2 -> distanceMeasure.calculate(v, v2)));
   }

   /**
    * Flattens a cluster down to a single list of vectors
    *
    * @param c the cluster
    * @return the list of vectors
    */
   protected List<NDArray> flatten(Cluster c) {
      if (c == null) {
         return Collections.emptyList();
      }
      if (!c.getPoints().isEmpty()) {
         return c.getPoints();
      }
      List<NDArray> list = new ArrayList<>();
      list.addAll(flatten(c.getLeft()));
      list.addAll(flatten(c.getRight()));
      return list;
   }


}//END OF Linkage
