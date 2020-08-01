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

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.VariableCollectionSequence;
import com.gengoai.apollo.ml.observation.VariableList;

import java.util.List;

/**
 * <p>
 * A featurizer extractor converts an input object into an {@link Observation}.
 * Specializations of this class are {@link Featurizer} that extract features based on a single object and {@link
 * ContextFeaturizer} which extract features based on the objects and its context.
 * </p>
 *
 * @param <I> the type parameter for the object being converted to an example.
 * @author David B. Bracewell
 */
public interface FeatureExtractor<I> extends ObservationExtractor<I> {

   /**
    * Applies only the contextual extractors to the given sequence.
    *
    * @param sequence the sequence to generate contextual features for
    * @return the example with contextual features
    */
   default VariableCollectionSequence contextualize(VariableCollectionSequence sequence) {
      return sequence;
   }

   @Override
   VariableList extractObservation(I input);

   @Override
   default VariableCollectionSequence extractSequence(List<? extends I> sequence) {
      VariableCollectionSequence out = new VariableCollectionSequence();
      for(I i : sequence) {
         out.add(extractObservation(i));
      }
      return contextualize(out);
   }

}//END OF FeatureExtractor
