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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.collection.HashBasedTable;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Sets;
import com.gengoai.collection.Table;
import com.gengoai.conversion.Cast;

import java.io.Serializable;
import java.util.*;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.Validation.notNull;

/**
 * Default graph implementation that uses a {@link Table} to store edges and keeps track of the vertices in a {@link
 * Set}. By default a {@link LinkedHashSet} is used so that the order of the vertices is the same as when added.
 *
 * @author David B. Bracewell
 */
public class DefaultGraphImpl<V> implements com.gengoai.graph.Graph<V>, Serializable {

   private static final long serialVersionUID = 2648221581604458992L;
   private final com.gengoai.graph.EdgeFactory<V> edgeFactory;
   @JsonProperty("v")
   private final Set<V> vertices;
   private final Table<V, V, com.gengoai.graph.Edge<V>> matrix;

   /**
    * Instantiates a new Adjacency matrix.
    *
    * @param edgeFactory the edge factory
    */
   public DefaultGraphImpl(com.gengoai.graph.EdgeFactory<V> edgeFactory) {
      this(notNull(edgeFactory), new LinkedHashSet<>(), new HashBasedTable<>());
   }

   /**
    * Instantiates a new Adjacency matrix.
    *
    * @param edgeFactory the edge factory
    * @param matrix      the matrix to use to back the map
    */
   private DefaultGraphImpl(com.gengoai.graph.EdgeFactory<V> edgeFactory,
                            Set<V> vertices,
                            Table<V, V, com.gengoai.graph.Edge<V>> matrix) {
      this.edgeFactory = notNull(edgeFactory);
      this.matrix = notNull(matrix);
      this.vertices = notNull(vertices);
   }

   private DefaultGraphImpl(@JsonProperty("ef") com.gengoai.graph.EdgeFactory<V> edgeFactory,
                            @JsonProperty("v") Set<V> vertices,
                            @JsonProperty("e") List<? extends Edge<V>> edges
                           ) {
      this(edgeFactory, vertices, new HashBasedTable<>());
      for(Edge<V> edge : edges) {
         addEdge(edge);
      }
   }

   @Override
   public com.gengoai.graph.Edge<V> addEdge(V fromVertex, V toVertex) {
      return addEdge(fromVertex, toVertex, 1d);
   }

   @Override
   public com.gengoai.graph.Edge<V> addEdge(V fromVertex, V toVertex, double weight) {
      checkArgument(containsVertex(fromVertex), "Source vertex must exist in the graph");
      checkArgument(containsVertex(toVertex), "Destination vertex must exist in the graph");
      com.gengoai.graph.Edge<V> edge = edgeFactory.createEdge(fromVertex, toVertex, weight);
      addEdge(edge);
      return edge;
   }

   @Override
   public void addEdge(com.gengoai.graph.Edge<V> edge) {
      notNull(edge);
      checkArgument(containsVertex(edge.vertex1), "Source vertex must exist in the graph");
      checkArgument(containsVertex(edge.vertex2), "Destination vertex must exist in the graph");
      checkArgument(!containsEdge(edge), "Edge already exists");
      checkArgument(edgeFactory.getEdgeClass().isAssignableFrom(edge.getClass()),
                    () -> "Graph only supports " + edgeFactory.getEdgeClass().getName() + " edges.");
      V fromVertex = edge.getFirstVertex();
      V toVertex = edge.getSecondVertex();
      matrix.put(fromVertex, toVertex, edge);
      if(!edge.isDirected()) {
         matrix.put(toVertex, fromVertex, edge);
      }
   }

   @Override
   public boolean addVertex(V vertex) {
      return vertices.add(notNull(vertex));
   }

   @Override
   public void addVertices(Collection<V> vertices) {
      this.vertices.addAll(vertices);
   }

   @Override
   public boolean containsEdge(V fromVertex, V toVertex) {
      return matrix.contains(fromVertex, toVertex);
   }

   @Override
   public boolean containsVertex(V vertex) {
      return vertices.contains(vertex);
   }

   @Override
   public int degree(V vertex) {
      if(isDirected()) {
         return matrix.row(vertex).size() + matrix.column(vertex).size();
      }
      return matrix.row(vertex).size();
   }

   @Override
   @JsonProperty("e")
   public Set<? extends com.gengoai.graph.Edge<V>> edges() {
      return Sets.asHashSet(matrix.values());
   }

   @Override
   public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof DefaultGraphImpl)) return false;
      DefaultGraphImpl<?> that = (DefaultGraphImpl<?>) o;
      return Objects.equals(edgeFactory.getClass(), that.edgeFactory.getClass())
            && Objects.equals(vertices, that.vertices) &&
            Objects.equals(matrix, that.matrix);
   }

   @Override
   public com.gengoai.graph.Edge<V> getEdge(V v1, V v2) {
      return Cast.as(matrix.get(v1, v2));
   }

   @Override
   public com.gengoai.graph.EdgeFactory<V> getEdgeFactory() {
      return edgeFactory;
   }

   @Override
   public Set<? extends com.gengoai.graph.Edge<V>> getInEdges(V vertex) {
      return Sets.asHashSet(matrix.column(vertex).values());

   }

   @Override
   public Set<? extends com.gengoai.graph.Edge<V>> getOutEdges(V vertex) {
      return Sets.asHashSet(matrix.row(vertex).values());
   }

   @Override
   public Set<V> getPredecessors(V vertex) {
      return Sets.asHashSet(matrix.column(vertex).keySet());
   }

   @Override
   public Set<V> getSuccessors(V vertex) {
      return Sets.asHashSet(matrix.row(vertex).keySet());
   }

   @Override
   public int hashCode() {
      return Objects.hash(vertices, matrix);
   }

   @Override
   public int inDegree(V vertex) {
      return matrix.column(vertex).size();
   }

   @Override
   public boolean isDirected() {
      return edgeFactory.isDirected();
   }

   @Override
   public boolean isEmpty() {
      return vertices.isEmpty();
   }

   @Override
   public Iterator<V> iterator() {
      return Iterators.unmodifiableIterator(vertices.iterator());
   }

   @Override
   public int numberOfEdges() {
      return isDirected()
             ? matrix.size()
             : matrix.size() / 2;
   }

   @Override
   public int numberOfVertices() {
      return vertices.size();
   }

   @Override
   public int outDegree(V vertex) {
      return matrix.row(vertex).size();
   }

   @Override
   public com.gengoai.graph.Edge<V> removeEdge(V fromVertex, V toVertex) {
      com.gengoai.graph.Edge<V> edge = Cast.as(matrix.remove(fromVertex, toVertex));
      if(edge != null && !isDirected()) {
         matrix.remove(toVertex, fromVertex);
      }
      return edge;
   }

   @Override
   public boolean removeEdge(com.gengoai.graph.Edge<V> edge) {
      notNull(edge);
      return removeEdge(edge.getFirstVertex(), edge.getSecondVertex()) != null;
   }

   @Override
   public boolean removeVertex(V vertex) {
      if(vertices.contains(vertex)) {
         vertices.remove(vertex);
         matrix.row(vertex).clear();
         matrix.column(vertex).clear();
         return true;
      }
      return false;
   }

   @Override
   public String toString() {
      return "Graph{numVertices=" + numberOfVertices() + ", numEdges=" + numberOfEdges() + "}";
   }

   @Override
   public Set<V> vertices() {
      return Collections.unmodifiableSet(vertices);
   }
}//END OF AdjacencyMatrix
