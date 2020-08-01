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
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Converts an raw object into an {@link Observation}. Common usage for extractor features (see {@link
 * com.gengoai.apollo.ml.feature.FeatureExtractor} and labels from data.
 * </p>
 *
 * @param <T> the input type parameter
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface ObservationExtractor<T> extends Serializable {

   /**
    * Extracts information from the given raw input object creating an {@link Observation} describing the information.
    *
    * @param input the input
    * @return the observation
    */
   Observation extractObservation(@NonNull T input);

   /**
    * Generates a sequence of observations based a list of given raw inputs
    *
    * @param sequence the sequence
    * @return the observation
    */
   default Observation extractSequence(@NonNull List<? extends T> sequence) {
      List<Observation> instances = new ArrayList<>();
      sequence.forEach(t -> instances.add(extractObservation(t)));
      if(instances.size() == 0) {
         return new VariableSequence();
      }
      if(instances.get(0).isVariableCollection()) {
         return new VariableCollectionSequence(Cast.cast(instances));
      }
      return new VariableSequence(Cast.cast(instances));
   }

}//END OF ObservationGenerator
