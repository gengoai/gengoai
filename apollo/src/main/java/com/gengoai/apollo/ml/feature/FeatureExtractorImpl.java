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

package com.gengoai.apollo.ml.feature;

import com.gengoai.apollo.ml.observation.VariableCollectionSequence;
import com.gengoai.apollo.ml.observation.VariableList;

import java.io.Serializable;

/**
 * <p>
 * A feature extractor that contains both a featurizer and a context featurizer.
 * </p>
 *
 * @param <I> the type parameter
 * @author David B. Bracewell
 */
class FeatureExtractorImpl<I> implements FeatureExtractor<I>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Featurizer<I> featurizer;
   private final ContextFeaturizer<I> contextFeaturizer;

   /**
    * Instantiates a new Feature extractor.
    *
    * @param featurizer        the featurizer
    * @param contextFeaturizer the context featurizer
    */
   public FeatureExtractorImpl(Featurizer<I> featurizer, ContextFeaturizer<I> contextFeaturizer) {
      this.featurizer = featurizer;
      this.contextFeaturizer = contextFeaturizer;
   }

   @Override
   public VariableCollectionSequence contextualize(VariableCollectionSequence sequence) {
      return contextFeaturizer.contextualize(sequence);
   }

   @Override
   public VariableList extractObservation(I input) {
      return featurizer.extractObservation(input);
   }

   @Override
   public String toString() {
      return featurizer.toString() + contextFeaturizer.toString();
   }

}//END OF FeatureExtractorImpl
