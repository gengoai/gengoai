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

package com.gengoai.apollo.ml.model;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.ObservationMetadata;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.SingleSourceTransform;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * A base implementation of a {@link Model} that acts as a {@link SingleSourceTransform}. Concrete implementations have
 * access to the <code>input</code> and <code>output</code> names and must implement the following methods:
 * </p>
 * <p><ul>
 * <li>{@link #transform(Observation)} - transforms an {@link Observation} from its input source</li>
 * <li>{@link #updateMetadata(DataSet)} - updates the {@link ObservationMetadata} associated
 * with the {@link DataSet}, which may require updating both the input and output source metadata.</li>
 * </ul></p>
 *
 * @author David B. Bracewell
 */
public abstract class SingleSourceModel<F extends SingleSourceFitParameters<F>, T extends SingleSourceModel<F, T>> implements Model, SingleSourceTransform {
   private static final long serialVersionUID = 1L;
   protected final F parameters;

   protected SingleSourceModel(@NonNull F parameters) {
      this.parameters = parameters;
   }

   @Override
   public F getFitParameters() {
      return parameters;
   }

   @Override
   public final Set<String> getInputs() {
      return Collections.singleton(parameters.input.value());
   }

   @Override
   public final Set<String> getOutputs() {
      return Collections.singleton(parameters.output.value());
   }

   @Override
   public T input(@NonNull String name) {
      parameters.input.set(Validation.notNullOrBlank(name));
      if(Strings.isNotNullOrBlank(parameters.output.value())) {
         parameters.output.set(name);
      }
      return Cast.as(this);
   }

   @Override
   public T output(@NonNull String name) {
      parameters.output.set(Validation.notNullOrBlank(name));
      return Cast.as(this);
   }

   @Override
   public T source(@NonNull String name) {
      parameters.input.set(Validation.notNullOrBlank(name));
      parameters.output.set(name);
      return Cast.as(this);
   }

   @Override
   public final DataSet transform(@NonNull DataSet dataset) {
      DataSet data = dataset.map(this::transform);
      updateMetadata(data);
      return data;
   }

   @Override
   public final Datum transform(@NonNull Datum datum) {
      datum.put(parameters.output.value(),
                transform(datum.get(parameters.input.value())));
      return datum;
   }

   /**
    * Transforms the given observation.
    *
    * @param observation the observation to transform
    * @return the transformed observation
    */
   protected abstract Observation transform(@NonNull Observation observation);

   /**
    * Updates the {@link ObservationMetadata} for the input and output as needed.
    *
    * @param data the data
    */
   protected abstract void updateMetadata(@NonNull DataSet data);

}//END OF SingleSourceModel
