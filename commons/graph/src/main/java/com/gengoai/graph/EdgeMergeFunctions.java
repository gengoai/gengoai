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
 */

package com.gengoai.graph;

/**
 * A set of common edge merge functions.
 *
 * @author David B. Bracewell
 */
public final class EdgeMergeFunctions {

   /**
    * A merge function that keeps the original edge and ignores the duplicate.
    *
    * @param <V> The vertex type
    * @return An EdgeMergeFunction
    */
   public static <V> EdgeMergeFunction<V> keepOriginal() {
      return (originalEdge, duplicateEdge, factory) -> originalEdge;
   }


   /**
    * A merge function that keeps the duplicate edge and ignores the original.
    *
    * @param <V> The vertex type
    * @return An EdgeMergeFunction
    */
   public static <V> EdgeMergeFunction<V> keepDuplicate() {
      return (originalEdge, duplicateEdge, factory) -> duplicateEdge;
   }

   /**
    * A merge function that creates a new edge whose weight is the average of the two edges.
    *
    * @param <V> The vertex type
    * @return An EdgeMergeFunction
    */
   public static <V> EdgeMergeFunction<V> averageWeight() {
      return (originalEdge, duplicateEdge, factory) -> factory.createEdge(
         originalEdge.getFirstVertex(),
         originalEdge.getSecondVertex(),
         (originalEdge.getWeight() + duplicateEdge.getWeight()) / 2d
                                                                         );
   }

   /**
    * A merge function that creates a new edge whose weight is the minimum of the two edges.
    *
    * @param <V> The vertex type
    * @return An EdgeMergeFunction
    */
   public static <V> EdgeMergeFunction<V> minWeight() {
      return (originalEdge, duplicateEdge, factory) -> factory.createEdge(
         originalEdge.getFirstVertex(),
         originalEdge.getSecondVertex(),
         Math.min(originalEdge.getWeight(), duplicateEdge.getWeight())
                                                                         );
   }

   /**
    * A merge function that creates a new edge whose weight is the maximum of the two edges.
    *
    * @param <V> The vertex type
    * @return An EdgeMergeFunction
    */
   public static <V> EdgeMergeFunction<V> maxWeight() {
      return (originalEdge, duplicateEdge, factory) -> factory.createEdge(
         originalEdge.getFirstVertex(),
         originalEdge.getSecondVertex(),
         Math.max(originalEdge.getWeight(), duplicateEdge.getWeight())
                                                                         );
   }

}//END OF EdgeMergeFunctions
