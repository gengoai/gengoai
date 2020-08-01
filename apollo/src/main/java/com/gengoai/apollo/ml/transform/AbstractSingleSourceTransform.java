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

package com.gengoai.apollo.ml.transform;

import com.gengoai.Copyable;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.ObservationMetadata;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * A base implementation of a {@link SingleSourceTransform}. Concrete implementations have access to the
 * <code>input</code> and <code>output</code> names and must implement the following methods:
 * </p>
 * <p><ul>
 * <li>{@link #fit(MStream)} - fits the transform to a stream of {@link Observation} from its input source</li>
 * <li>{@link #transform(Observation)} - transforms an {@link Observation} from its input source</li>
 * <li>{@link #updateMetadata(DataSet)} - updates the {@link ObservationMetadata} associated with
 * the {@link DataSet}, which may require updating both the input and output source metadata.</li>
 * </ul></p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class AbstractSingleSourceTransform<T extends AbstractSingleSourceTransform<T>> implements Transform, SingleSourceTransform {
   private static final long serialVersionUID = 1L;
   /**
    * The name of the input source
    */
   @NonNull
   protected String input = Datum.DEFAULT_INPUT;
   /**
    * The name of the output source
    */
   @NonNull
   protected String output = Datum.DEFAULT_INPUT;
   protected NDArrayFactory ndArrayFactory;

   public AbstractSingleSourceTransform() {
      this.ndArrayFactory = NDArrayFactory.ND;
   }

   @Override
   public T copy() {
      return Cast.as(Copyable.deepCopy(this));
   }

   /**
    * fits the transform to a stream of {@link Observation} from its input source
    *
    * @param observations the stream of observations from the input source
    */
   protected abstract void fit(@NonNull MStream<Observation> observations);

   @Override
   public DataSet fitAndTransform(DataSet dataset) {
      ndArrayFactory = dataset.getNDArrayFactory();
      fit(dataset.stream().map(d -> d.get(input)));
      return transform(dataset);
   }

   @Override
   public final Set<String> getInputs() {
      return Collections.singleton(input);
   }

   @Override
   public final Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   @Override
   public T input(@NonNull String name) {
      this.input = name;
      if(Strings.isNotNullOrBlank(output)) {
         this.output = name;
      }
      return Cast.as(this);
   }

   @Override
   public T output(@NonNull String name) {
      this.output = name;
      return Cast.as(this);
   }

   @Override
   public T source(@NonNull String name) {
      this.input = name;
      this.output = name;
      return Cast.as(this);
   }

   @Override
   public final DataSet transform(@NonNull DataSet dataset) {
      DataSet data = dataset.map(this::transform);
      updateMetadata(data);
      return data;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      datum.put(output, transform(datum.get(input)));
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
}//END OF SingleSourceDatumTransform
