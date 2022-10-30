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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.Streams;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>Interface defining a graph data structure.</p>
 *
 * @param <V> the vertex type
 * @author David B. Bracewell
 */
@JsonDeserialize(as = DefaultGraphImpl.class)
public interface Graph<V> extends Iterable<V> {

   /**
    * Creates a graph with vertices <code>V</code> and edges defined by the given {@link EdgeFactory}.
    *
    * @param <V>         the vertex type parameter
    * @param edgeFactory the edge factory to use for creating and validating edges
    * @return the graph
    */
   static <V> Graph<V> create(EdgeFactory<V> edgeFactory) {
      return new DefaultGraphImpl<>(edgeFactory);
   }

   /**
    * Creates a graph with vertices <code>V</code> and edges defined by {@link DirectedEdgeFactory}.
    *
    * @param <V> the vertex type parameter
    * @return the graph
    */
   static <V> Graph<V> directed() {
      return new DefaultGraphImpl<>(new DirectedEdgeFactory<>());
   }

   /**
    * Creates a graph with vertices <code>V</code> and edges defined by {@link UndirectedEdgeFactory}.
    *
    * @param <V> the vertex type parameter
    * @return the graph
    */
   static <V> Graph<V> undirected() {
      return new DefaultGraphImpl<>(new UndirectedEdgeFactory<>());
   }

   /**
    * Adds an edge to the graph.
    *
    * @param edge the edge
    */
   void addEdge(Edge<V> edge);

   /**
    * Adds an edge to the graph
    *
    * @param fromVertex The first (from) vertex
    * @param toVertex   The second (to) vertex
    * @return The edge that was created
    */
   Edge<V> addEdge(V fromVertex, V toVertex);

   /**
    * Adds an edge to the graph
    *
    * @param fromVertex The first (from) vertex
    * @param toVertex   The second (to) vertex
    * @param weight     The weight of the edge
    * @return The edge that was created
    */
   Edge<V> addEdge(V fromVertex, V toVertex, double weight);

   /**
    * Adds a collection of edges to the graph.
    *
    * @param edges the edges
    */
   default void addEdges(Collection<? extends Edge<V>> edges) {
      if(edges != null) {
         edges.stream().filter(e -> !containsEdge(e)).forEach(this::addEdge);
      }
   }

   /**
    * Adds a vertex to the graph
    *
    * @param vertex The vertex
    * @return True if the vertex was added, False if not
    */
   boolean addVertex(V vertex);

   /**
    * Adds all vertices in a collection to the graph
    *
    * @param vertices The vertices to add
    */
   void addVertices(Collection<V> vertices);

   /**
    * Checks if an edge in the graph
    *
    * @param fromVertex The first (from) vertex
    * @param toVertex   The second (to) vertex
    * @return True if the edge is in the graph, false if not
    */
   boolean containsEdge(V fromVertex, V toVertex);

   /**
    * Checks if an edge in the graph
    *
    * @param edge The edge to check
    * @return True if the edge is in the graph, false if not
    */
   default boolean containsEdge(Edge<V> edge) {
      if(edge != null) {
         return containsEdge(edge.vertex1, edge.vertex2);
      }
      return false;
   }

   /**
    * Checks if a vertex in the graph
    *
    * @param vertex The vertex
    * @return True if the vertex is in the graph, false if not
    */
   boolean containsVertex(V vertex);

   /**
    * The number of neighbors
    *
    * @param vertex The vertex
    * @return The degree
    */
   int degree(V vertex);

   /**
    * Edges set.
    *
    * @return The set of edges in the graph
    */
   Set<? extends Edge<V>> edges();

   /**
    * Gets the edge if one, between the two given vertices
    *
    * @param v1 vertex 1
    * @param v2 vertex 2
    * @return The edge if one, null otherwise
    */
   Edge<V> getEdge(V v1, V v2);

   /**
    * Gets the edge factory used in this grapy
    *
    * @return The edge factory
    */
   @JsonProperty("ef")
   EdgeFactory<V> getEdgeFactory();

   /**
    * Gets all edges incident to the given vertex.
    *
    * @param vertex The vertex
    * @return The set of incident edges
    */
   default Set<? extends Edge<V>> getEdges(V vertex) {
      return Cast.cast(Sets.union(getOutEdges(vertex), getInEdges(vertex)));
   }

   /**
    * Gets the edges coming in to the given vertex (i.e. in-links)
    *
    * @param vertex The vertex
    * @return The set of incoming edges
    */
   Set<? extends Edge<V>> getInEdges(V vertex);

   /**
    * Gets the set of vertices that share an edge with the given vertex
    *
    * @param vertex The vertex
    * @return The set of vertices which share an edge with the given vertex.
    */
   default Set<V> getNeighbors(V vertex) {
      return Sets.union(getPredecessors(vertex), getSuccessors(vertex));
   }

   /**
    * Gets the edges coming out out of the given vertex (i.e. out-links)
    *
    * @param vertex The vertex
    * @return The set of outgoing edges
    */
   Set<? extends Edge<V>> getOutEdges(V vertex);

   /**
    * Gets the neighbors associated with the incoming edges for the given vertex.
    *
    * @param vertex The vertex
    * @return The set of vertices which contain an outgoing edge to the given vertex.
    */
   Set<V> getPredecessors(V vertex);

   /**
    * Gets the weights associated with the edges to the predecessors of the given vertex.
    *
    * @param vertex The vertex
    * @return The weights associated with the edges to the predecessors
    */
   default Counter<V> getPredecessorsWeights(V vertex) {
      Counter<V> counter = Counters.newCounter();
      for(V v2 : getPredecessors(vertex)) {
         counter.set(v2, getEdge(vertex, v2).getWeight());
      }
      return counter;
   }

   /**
    * Gets the weights associated with the edges to the successors of the given vertex.
    *
    * @param vertex The vertex
    * @return The weights associated with the edges to the successors
    */
   default Counter<V> getSuccessorWeights(V vertex) {
      Counter<V> counter = Counters.newCounter();
      for(V v2 : getSuccessors(vertex)) {
         counter.set(v2, getEdge(vertex, v2).getWeight());
      }
      return counter;
   }

   /**
    * Gets the neighbors associated with the outgoing edges for the given vertex.
    *
    * @param vertex The vertex
    * @return The set of vertices which contain an incoming edge from the given vertex.
    */
   Set<V> getSuccessors(V vertex);

   /**
    * Gets the weight of the edge if one, between the two given vertices
    *
    * @param v1 vertex 1
    * @param v2 vertex 2
    * @return The weight if one, 0 otherwise
    */
   default double getWeight(V v1, V v2) {
      Edge<V> edge = getEdge(v1, v2);
      return edge == null
             ? 0
             : edge.getWeight();
   }

   /**
    * Gets the weights associated with the edges of the given vertex.
    *
    * @param vertex The vertex
    * @return The weights associated with the edges
    */
   default Counter<V> getWeights(V vertex) {
      Counter<V> weights = getSuccessorWeights(vertex);
      if(isDirected()) {
         weights.merge(getPredecessorsWeights(vertex));
      }
      return weights;
   }

   /**
    * The number of predecessors
    *
    * @param vertex The vertex
    * @return The in degree
    */
   int inDegree(V vertex);

   /**
    * Is directed.
    *
    * @return True if the graph is directed, false if it is undirected
    */
   @JsonIgnore
   boolean isDirected();

   /**
    * Determines if the graph is empty (no vertices no edges)
    *
    * @return True if the graph is empty
    */
   @JsonIgnore
   boolean isEmpty();

   /**
    * Merges another graph into this one ignoring any duplicate edges
    *
    * @param other The graph to merge
    */
   default void merge(Graph<V> other) {
      merge(other, EdgeMergeFunctions.keepOriginal());
   }

   /**
    * Merges another graph into this one combining edges using the supplied merge function
    *
    * @param other         The graph to merge
    * @param mergeFunction The function to use to merge duplicate edges
    */
   default void merge(Graph<V> other, EdgeMergeFunction<V> mergeFunction) {
      if(other == null || other.isEmpty()) {
         return;
      }
      if(this.isEmpty()) {
         this.addVertices(other.vertices());
         this.addEdges(other.edges());
      } else {
         this.addVertices(other.vertices());
         for(Edge<V> edge : other.edges()) {
            if(this.containsEdge(edge)) {
               Edge<V> orig = this.removeEdge(edge.getFirstVertex(), edge.getSecondVertex());
               this.addEdge(mergeFunction.merge(orig, edge, getEdgeFactory()));
            } else {
               this.addEdge(edge);
            }
         }
      }
   }

   /**
    * Number of edges.
    *
    * @return Number of edges in the graph
    */
   int numberOfEdges();

   /**
    * Number of vertices.
    *
    * @return Number of vertices in the graph
    */
   int numberOfVertices();

   /**
    * The number of successors
    *
    * @param vertex The vertex
    * @return The out degree
    */
   int outDegree(V vertex);

   /**
    * Returns a stream of the vertices in this graph
    *
    * @return A stream of the vertices in this graph
    */
   default Stream<V> parallelstream() {
      return Streams.asParallelStream(this);
   }

   /**
    * Removes an edge from the graph
    *
    * @param fromVertex The first (from) vertex
    * @param toVertex   The second (to) vertex
    * @return The edge that was removed or null if there was no edge
    */
   Edge<V> removeEdge(V fromVertex, V toVertex);

   /**
    * Removes an edge from the graph
    *
    * @param edge The edge to remove
    * @return True if the edge was removed, false if not
    */
   boolean removeEdge(Edge<V> edge);

   /**
    * Removes a vertex from the graph
    *
    * @param vertex The vertex to remove
    * @return True if the vertex was removed, false if not
    */
   boolean removeVertex(V vertex);

   /**
    * Returns a stream of the vertices in this graph
    *
    * @return A stream of the vertices in this graph
    */
   default Stream<V> stream() {
      return Streams.asStream(this);
   }

   /**
    * Vertices set.
    *
    * @return The set of vertices in the graph
    */
   Set<V> vertices();

}//END OF Graph
