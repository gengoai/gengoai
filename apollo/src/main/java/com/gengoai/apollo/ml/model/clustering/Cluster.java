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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A cluster is set of points that are closer in feature space to one another than other points.
 * </p>
 *
 * @author David B. Bracewell
 */
public class Cluster implements Serializable, Iterable<NDArray> {
   private static final long serialVersionUID = 1L;
   private final List<NDArray> points = new ArrayList<>();
   private NDArray centroid;
   private int id;
   private Cluster left;
   private Cluster parent;
   private Cluster right;
   private double score;

   /**
    * Adds a point to the cluster
    *
    * @param point the point
    */
   public void addPoint(NDArray point) {
      this.points.add(point);
   }

   /**
    * Clears the cluster, removing all points
    */
   public void clear() {
      points.clear();
   }

   /**
    * Gets the centroid (the center of mass) of the cluster.
    *
    * @return the centroid
    */
   public NDArray getCentroid() {
      return centroid;
   }

   /**
    * Gets the cluster id.
    *
    * @return the id
    */
   public int getId() {
      return id;
   }

   /**
    * Gets the left child cluster of this one in hierarchical clusters.
    *
    * @return the left child
    */
   public Cluster getLeft() {
      return left;
   }

   /**
    * Gets parent cluster of this one in hierarchical clusters.
    *
    * @return the parent cluster
    */
   public Cluster getParent() {
      return parent;
   }

   /**
    * Gets the points in this cluster.
    *
    * @return the points in the cluster
    */
   public List<NDArray> getPoints() {
      return points;
   }

   /**
    * Gets the right child cluster of this one in hierarchical clusters.
    *
    * @return the right child
    */
   public Cluster getRight() {
      return right;
   }

   /**
    * Gets the score of the cluster.
    *
    * @return the cluster score
    */
   public double getScore() {
      return score;
   }

   @Override
   public Iterator<NDArray> iterator() {
      return points.iterator();
   }

   /**
    * Sets the centroid (the center of mass) of the cluster.
    *
    * @param centroid the centroid
    */
   public void setCentroid(NDArray centroid) {
      this.centroid = centroid;
   }

   /**
    * Sets the cluster id.
    *
    * @param id the id
    */
   public void setId(int id) {
      this.id = id;
   }

   /**
    * Sets the left child cluster of this one in hierarchical clusters.
    *
    * @param left the left child
    */
   public void setLeft(Cluster left) {
      this.left = left;
   }

   /**
    * Sets parent cluster of this one in hierarchical clusters.
    *
    * @param parent the parent cluster
    */
   public void setParent(Cluster parent) {
      this.parent = parent;
   }

   /**
    * Sets the right child cluster of this one in hierarchical clusters.
    *
    * @param right the right child
    */
   public void setRight(Cluster right) {
      this.right = right;
   }

   /**
    * Sets the score of the cluster.
    *
    * @param score the cluster score
    */
   public void setScore(double score) {
      this.score = score;
   }

   /**
    * The number of points in the cluster
    *
    * @return the number of points in the cluster
    */
   public int size() {
      return points.size();
   }

   @Override
   public String toString() {
      return "Cluster(id=" + id + ", size=" + points.size() + ")";
   }

}//END OF Cluster
