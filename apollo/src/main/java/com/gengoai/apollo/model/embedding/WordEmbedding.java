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

package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.math.linalg.*;
import com.gengoai.apollo.math.linalg.compose.VectorComposition;
import com.gengoai.apollo.math.linalg.compose.VectorCompositions;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Sequence;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.transform.Transform;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * A mapping of words (strings) into a vector representation where words with similar meaning are closer in vector
 * space.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class WordEmbedding implements Transform {
   private static final long serialVersionUID = 1L;
   protected KeyedVectorStore vectorStore;

   /**
    * Creates a vector using the given vector composition for the given words.
    *
    * @param composition the composition function to use
    * @param words       the words whose vectors we want to compose
    * @return a composite vector consisting of the given words and calculated using the given vector composition
    */
   public final NumericNDArray compose(@NonNull VectorComposition composition, @NonNull String... words) {
      if (words == null) {
         return nd.DFLOAT32.zeros(dimension());
      } else if (words.length == 1) {
         return embed(words[0]);
      }
      return composition.compose(Arrays.stream(words)
                                       .map(this::embed)
                                       .collect(Collectors.toList()));
   }

   public boolean contains(@NonNull String key) {
      return vectorStore.getAlphabet().contains(key);
   }

   /**
    * The dimension of the vectors in the store
    *
    * @return the dimension of the vectors
    */
   public final int dimension() {
      return vectorStore.dimension();
   }

   /**
    * Looks up the NDArray associated with the given feature name
    *
    * @param feature the feature name whose NDArray we want
    * @return the NDArray for the given feature, zero vector or unknown vector if feature is not valid.
    */
   public final NumericNDArray embed(@NonNull String feature) {
      return vectorStore.getVector(feature);
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      return transform(dataset);
   }

   /**
    * Gets the alphabet of words that are known
    *
    * @return the alphabet
    */
   public final Set<String> getAlphabet() {
      return vectorStore.getAlphabet();
   }

   /**
    * Translates a variable into a string
    *
    * @param v the variable
    * @return the variable name
    */
   protected String getVariableName(Variable v) {
      return v.getSuffix();
   }

   /**
    * Queries the vector store to find similar vectors to the given {@link VSQuery}.
    *
    * @param query the query to use find similar vectors
    * @return Stream of vectors matching the query
    */
   public final Stream<NumericNDArray> query(@NonNull VSQuery query) {
      NumericNDArray queryVector = query.queryVector(this);
      return query.applyFilters(vectorStore.stream()
                                           .parallel()
                                           .map(v -> v.copy().setWeight(query.measure().calculate(v, queryVector))));
   }

   /**
    * The number of vectors in the embedding
    *
    * @return the number of vectors in the embedding
    */
   public final int size() {
      return vectorStore.size();
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      for (String source : getInputs()) {
         datum.put(source, transform(datum.get(source)));
      }
      return datum;
   }

   protected NumericNDArray transform(Observation o) {
      if (o.isVariable()) {
         return embed(getVariableName(o.asVariable()));
      } else if (o.isVariableCollection()) {
         if (o.getVariableSpace().count() == 0) {
            return nd.DFLOAT32.zeros(1, dimension());
         }
         return VectorCompositions.Average.compose(o.asVariableCollection()
                                                    .getVariableSpace()
                                                    .map(v -> embed(getVariableName(v)))
                                                    .collect(Collectors.toList()));
      } else if (o.isSequence()) {
         Sequence<?> sequence = o.asSequence();
         List<NumericNDArray> vectors = new ArrayList<>();
         for (Observation observation : sequence) {
            vectors.add(transform(observation));
         }
         return nd.vstack(vectors);
      }
      throw new IllegalArgumentException("Cannot transform Observations of type " + o.getClass());
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      dataset = dataset.map(this::transform);
      for (String output : getOutputs()) {
         dataset.updateMetadata(output, m -> {
            m.setDimension(dimension());
            m.setType(NDArray.class);
            m.setEncoder(NoOptEncoder.INSTANCE);
         });
      }
      return dataset;
   }
}//END OF WordEmbedding
