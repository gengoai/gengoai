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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.math.statistics.measure.Similarity;
import com.gengoai.collection.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Fluent class for constructing vector space queries for use by {@link TrainableWordEmbedding}s.
 *
 * @author David B. Bracewell
 */
public final class VSQuery {
   private int K = Integer.MAX_VALUE;
   private Measure measure = Similarity.Cosine;
   private List<String> negativeTerms = new ArrayList<>();
   private List<NDArray> negativeVectors = new ArrayList<>();
   private List<String> positiveTerms = new ArrayList<>();
   private List<NDArray> positiveVectors = new ArrayList<>();
   private double threshold = Double.NEGATIVE_INFINITY;

   /**
    * Creates a new VSQuery where the query is a composite of a set of positive and negative example terms. The query is
    * created by creating a positive vector that is the sum of the associated vectors for the positive terms and a
    * negative vector that is the sum of the associated vectors for the negative terms. The query vector is then created
    * by <code>positive_vector - negative_vector</code>.
    *
    * @param positive the positive terms to query
    * @param negative the negative terms to query
    * @return A new VSQuery object
    */
   public static VSQuery compositeTermQuery(Iterable<String> positive, Iterable<String> negative) {
      return new VSQuery().positiveTerms(positive)
                          .negativeTerms(negative);
   }

   /**
    * Creates a new VSQuery where the query is a composite of a set of positive and negative example vectors. The query
    * is created by creating a positive vector that is the sum of the associated vectors for the positive terms and a
    * negative vector that is the sum of the associated vectors for the negative terms. The query vector is then created
    * by <code>positive_vector - negative_vector</code>.
    *
    * @param positive the positive vectors to query
    * @param negative the negative vectors to query
    * @return A new VSQuery object
    */
   public static VSQuery compositeVectorQuery(Iterable<NDArray> positive, Iterable<NDArray> negative) {
      return new VSQuery().positiveVectors(positive)
                          .negativeVectors(negative);
   }

   /**
    * Creates a new VSQuery that will query the vector store using the given term.
    *
    * @param term the term to query
    * @return A new VSQuery object
    */
   public static VSQuery termQuery(String term) {
      return new VSQuery().query(term);
   }

   /**
    * Creates a new VSQuery that will query the vector store using the given vector.
    *
    * @param vector the vector to query
    * @return A new VSQuery object
    */
   public static VSQuery vectorQuery(NDArray vector) {
      return new VSQuery().query(vector);
   }

   /**
    * Apply filters stream.
    *
    * @param stream the stream
    * @return the stream
    */
   public Stream<NDArray> applyFilters(Stream<NDArray> stream) {
      if(Double.isFinite(threshold())) {
         stream = stream.filter(v -> measure().getOptimum().test(v.getWeight(), threshold()));
      }
      stream = stream.sorted((v1, v2) -> measure().getOptimum().compare(v1.getWeight(), v2.getWeight()));
      Set<String> exclude = getExcludedLabels();
      if(exclude.size() > 0) {
         stream = stream.filter(v -> !exclude.contains(v.getLabel()));
      }
      if(limit() > 0 && limit() < Integer.MAX_VALUE) {
         stream = stream.limit(limit());
      }
      return stream;
   }

   /**
    * Clears the query terms and example vectors
    */
   public void clearQuery() {
      positiveVectors.clear();
      negativeVectors.clear();
      positiveTerms.clear();
      negativeTerms.clear();
   }

   /**
    * Gets any terms used for query that should be excluded when making searches.
    *
    * @return the excluded labels
    */
   public Set<String> getExcludedLabels() {
      return Sets.union(positiveTerms, negativeTerms);
   }

   private NDArray getVector(List<NDArray> vectors, List<String> terms, WordEmbedding store) {
      Stream<NDArray> stream;
      if(vectors.size() > 0) {
         stream = vectors.stream();
      } else {
         stream = terms.stream().map(store::embed);
      }
      return stream.reduce(NDArrayFactory.DENSE.array(store.dimension()), NDArray::addi);
   }

   /**
    * Sets the maximum number of results to return from the query.
    *
    * @param K the the maximum number of results to return from the query
    * @return This VSQuery
    */
   public VSQuery limit(int K) {
      this.K = K;
      return this;
   }

   /**
    * Gets the maximum number of results to return from the query.
    *
    * @return the maximum number of results to return from the query.
    */
   public int limit() {
      return K;
   }

   /**
    * Sets the measure to use for comparing returned vectors
    *
    * @param measure the measure
    * @return This VSQuery
    */
   public VSQuery measure(Measure measure) {
      this.measure = measure;
      return this;
   }

   /**
    * Gets the measure to use for comparing returned vectors
    *
    * @return the measure to use for comparing returned vectors
    */
   public Measure measure() {
      return measure;
   }

   /**
    * Sets the negative example terms to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery negativeTerms(String... terms) {
      negativeVectors.clear();
      negativeTerms.clear();
      Collections.addAll(negativeTerms, terms);
      return this;
   }

   /**
    * Sets the negative example terms to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery negativeTerms(Iterable<String> terms) {
      negativeVectors.clear();
      negativeTerms.clear();
      terms.forEach(negativeTerms::add);
      return this;
   }

   /**
    * Sets the negative example vectors to use for querying
    *
    * @param vectors the vectors
    * @return This VSQuery
    */
   public VSQuery negativeVectors(NDArray... vectors) {
      negativeVectors.clear();
      negativeTerms.clear();
      Collections.addAll(negativeVectors, vectors);
      return this;
   }

   /**
    * Sets the negative example vectors to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery negativeVectors(Iterable<NDArray> terms) {
      negativeVectors.clear();
      negativeTerms.clear();
      terms.forEach(negativeVectors::add);
      return this;
   }

   /**
    * Sets the positive example terms to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery positiveTerms(String... terms) {
      positiveVectors.clear();
      positiveTerms.clear();
      Collections.addAll(positiveTerms, terms);
      return this;
   }

   /**
    * Sets the positive example terms to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery positiveTerms(Iterable<String> terms) {
      positiveVectors.clear();
      positiveTerms.clear();
      terms.forEach(positiveTerms::add);
      return this;
   }

   /**
    * Sets the positive example vectors to use for querying
    *
    * @param vectors the vectors
    * @return This VSQuery
    */
   public VSQuery positiveVectors(NDArray... vectors) {
      positiveVectors.clear();
      positiveTerms.clear();
      Collections.addAll(positiveVectors, vectors);
      return this;
   }

   /**
    * Sets the positive example vectors to use for querying
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery positiveVectors(Iterable<NDArray> terms) {
      positiveVectors.clear();
      positiveTerms.clear();
      terms.forEach(positiveVectors::add);
      return this;
   }

   /**
    * Sets the vectors to use for querying
    *
    * @param vectors the vectors
    * @return This VSQuery
    */
   public VSQuery query(NDArray... vectors) {
      clearQuery();
      Collections.addAll(positiveVectors, vectors);
      return this;
   }

   /**
    * Sets the example terms to use for querying.
    *
    * @param terms the terms
    * @return This VSQuery
    */
   public VSQuery query(String... terms) {
      clearQuery();
      Collections.addAll(positiveTerms, terms);
      return this;
   }

   /**
    * Generates a query vector for the given {@link TrainableWordEmbedding}
    *
    * @param store the vector store that the final query vector will be for.
    * @return the query vector
    */
   public NDArray queryVector(WordEmbedding store) {
      NDArray pos = getVector(positiveVectors, positiveTerms, store);
      NDArray neg = getVector(negativeVectors, negativeTerms, store);
      return pos.subi(neg);
   }

   /**
    * Sets the threshold to use for eliminating vectors from the query.
    *
    * @param threshold the threshold
    * @return This VSQuery
    */
   public VSQuery threshold(double threshold) {
      this.threshold = threshold;
      return this;
   }

   /**
    * Gets the threshold to use for eliminating vectors from the query.
    *
    * @return the threshold to use for eliminating vectors from the query.
    */
   public double threshold() {
      return threshold;
   }

}//END OF VSQuery
