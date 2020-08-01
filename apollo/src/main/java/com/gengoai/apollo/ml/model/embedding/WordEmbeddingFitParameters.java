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

import com.gengoai.apollo.math.linalg.VectorComposition;
import com.gengoai.apollo.math.linalg.VectorCompositions;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.CombinableOutputFitParameters;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.observation.VariableNameSpace;

import java.util.Collections;
import java.util.Set;

/**
 * Specialized {@link FitParameters} for embeddings, which includes a supplier to generate builders for vector stores.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class WordEmbeddingFitParameters<T extends WordEmbeddingFitParameters<T>> extends CombinableOutputFitParameters<T> {
   private static final long serialVersionUID = 1L;
   /**
    * The set of inputs into the model. Note that if order is important use a LinkedHashSet. (default
    * Set(DEFAULT_INPUT))
    */
   public final Parameter<Set<String>> inputs = parameter(Params.inputs, Collections.singleton(Datum.DEFAULT_INPUT));
   /**
    * The dimension of the learned word embeddings (default 100).
    */
   public final Parameter<Integer> dimension = parameter(Params.Embedding.dimension, 100);
   /**
    * The window size for calculating cooccurrence information (default 10).
    */
   public final Parameter<Integer> windowSize = parameter(Params.Embedding.windowSize, 10);
   /**
    * The {@link VectorComposition} to use when combining outputs for multiple inputs (default {@link
    * VectorCompositions#Average}).
    */
   public final Parameter<VectorComposition> aggregationFunction = parameter(Params.Embedding.aggregationFunction,
                                                                             VectorCompositions.Average);
   /**
    * Determines the naming strategy for converting Variables into words (default Full).
    */
   public final Parameter<VariableNameSpace> nameSpace = parameter(Params.Embedding.nameSpace, VariableNameSpace.Full);
   /**
    * The word representing an unknown key in the vector store (default null).
    */
   public final Parameter<String> unknownWord = parameter(Params.Embedding.unknownWord, null);
   /**
    * Words representing special meaning in the the vector store (default String[0]).
    */
   public final Parameter<String[]> specialWords = parameter(Params.Embedding.specialWords, new String[0]);

}//END OF WordEmbeddingFitParameters
