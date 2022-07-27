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
 * Function defining how to merge duplicate edges when merging two graphs together.
 *
 * @author David B. Bracewell
 */
public interface EdgeMergeFunction<V> {

  /**
   * Merges the information related to two edges defined over the same vertices creating a new edge.
   *
   * @param originalEdge  The edge on the graph which is being merged to
   * @param duplicateEdge The edge on the graph which is being merged from
   * @param factory       The factory to use if a new edge is constructed
   * @return The merged edge
   */
  Edge<V> merge(Edge<V> originalEdge, Edge<V> duplicateEdge, EdgeFactory<V> factory);


}//END OF EdgeMergeFunction
