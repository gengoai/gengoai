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

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.statistics.measure.Measure;
import lombok.Getter;
import lombok.Setter;
import org.apache.mahout.math.list.DoubleArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>A hierarchical clustering which is defined using a tree structure.</p>.
 *
 * @author David B. Bracewell
 */
public class HierarchicalClustering implements Clustering {
   private static final long serialVersionUID = 1L;
   protected Cluster root;
   @Getter
   @Setter
   private Measure measure;

   /**
    * Converts the hierarchical clustering into a flat clustering using the given threshold. Each subtree whose
    * inter-cluster distance is less than the given threshold will be flattened into one cluster.
    *
    * @param threshold the threshold to determine how to flatten clusters
    * @return the flat clustering
    */
   public Clustering asFlat(double threshold) {
      FlatClustering clustering = new FlatClustering();
      clustering.setMeasure(measure);
      process(root, clustering, threshold);
      for(int i = 0; i < clustering.size(); i++) {
         NDArray centroid = NDArrayFactory.ND.array(clustering.get(i).getPoints().get(0).shape());
         clustering.get(i).getPoints().forEach(centroid::addi);
         centroid.divi(clustering.get(i).size());
         clustering.get(i).setCentroid(centroid);
      }
      return clustering;
   }

   /**
    * Calculates the given percentile over cluster scores
    *
    * @param percentile the percentile
    * @return the cluster score at the given percentile.
    */
   public double calculatePercentile(double percentile) {
      Validation.checkArgument(percentile > 0 && percentile <= 1, "Percentile must be > 0 and <= 1");
      DoubleArrayList distances = new DoubleArrayList();
      Queue<Cluster> queue = new LinkedList<>();
      queue.add(root);
      while(queue.size() > 0) {
         Cluster c = queue.remove();
         if(c != null) {
            distances.add(c.getScore());
            queue.add(c.getLeft());
            queue.add(c.getRight());
         }
      }
      distances.sort();
      int index = (int) Math.floor(distances.size() * percentile);
      return distances.size() > 0
             ? distances.get(index)
             : root.getScore();
   }

   @Override
   public Cluster get(int index) {
      if(index == 0) {
         return root;
      }
      throw new IndexOutOfBoundsException();
   }

   @Override
   public Cluster getRoot() {
      return root;
   }

   @Override
   public boolean isFlat() {
      return false;
   }

   @Override
   public boolean isHierarchical() {
      return true;
   }

   @Override
   public Iterator<Cluster> iterator() {
      return Collections.singleton(root).iterator();
   }

   private void process(Cluster c, FlatClustering flat, double threshold) {
      if(c == null) {
         return;
      }
      if(measure.getOptimum().test(c.getScore(), threshold)) {
         flat.add(c);
      } else {
         process(c.getLeft(), flat, threshold);
         process(c.getRight(), flat, threshold);
      }
   }

   @Override
   public int size() {
      return 1;
   }
}//END OF HierarchicalClustering
