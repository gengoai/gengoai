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

package com.gengoai.apollo.ml;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.feature.ObservationExtractor;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.function.SerializableFunction;
import com.gengoai.stream.MStream;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * A DataSet generator aids in creating multi input and output data sets through a builder pattern. Each observation
 * source (input or output) is assigned an {@link ObservationExtractor} that defines how to extract an {@link
 * Observation} from raw Object of type <code>T</code>. Optionally, a <code>toSequence</code> function can be defined
 * that translates the input Object into a List of <code>T</code>. The following example demonstrates generating a data
 * set for part-of-speech tagging from a String input specified as <code>word_pos word_pos word_pos...</code>
 * </p>
 * <pre>
 * {@code
 *   DataSet pos = DataSetGenerator.<String>builder()
 *                                 .defaultInput(Featurizer.valueFeaturizer("WORD", w->w),
 *                                              s -> Stream.of(s.split("\\s+")).map(w -> w.split("_")[0]).collect(Collectors.toList()))
 *                                 .defaultOutput(w -> Variable.binary(w),
 *                                              s -> Stream.of(s.split("\\s+")).map(w -> w.split("_")[1]).collect(Collectors.toList()))
 *                                 .build()
 *                                 .generate(<INPUT_DATA>)
 * }
 * </pre>
 * <p>
 * Note: When constructing a data set the metadata is not initially set. If the metadata is required directly after
 * construction, you must first call {@link DataSet#probe()}.
 * </p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class DataSetGenerator<T> implements SerializableFunction<T, Datum> {
   private static final long serialVersionUID = 1L;
   private final List<GeneratorInfo<T>> generators = new ArrayList<>();
   private final DataSetType dataSetType;

   /**
    * Creates a {@link Builder} for constructing a DataSetGenerator
    *
    * @param <T> the raw type parameter
    * @return the builder
    */
   public static <T> Builder<T> builder() {
      return new Builder<>();
   }

   protected DataSetGenerator(@NonNull DataSetType dataSetType,
                              @NonNull Collection<GeneratorInfo<T>> generators) {
      this.dataSetType = dataSetType;
      this.generators.addAll(generators);
   }

   @Override
   public Datum apply(T input) {
      Datum datum = new Datum();
      for(GeneratorInfo<T> generator : generators) {
         Observation observation;
         if(generator.toSequence == null) {
            observation = generator.extractor.extractObservation(input);
         } else {
            observation = generator.extractor.extractSequence(generator.toSequence.apply(input));
         }
         datum.put(generator.name, observation);
      }
      return datum;
   }

   /**
    * Generates a new DataSet stored in-memory from the given collection of data
    *
    * @param data the data
    * @return the DataSet
    */
   public DataSet generate(@NonNull Collection<? extends T> data) {
      return dataSetType.create(data.stream().map(this));
   }

   /**
    * Generates a new streaming DataSet from the given stream of data
    *
    * @param data the data
    * @return the DataSet
    */
   public DataSet generate(@NonNull MStream<? extends T> data) {
      return dataSetType.create(data.map(this));
   }

   @Value
   protected static class GeneratorInfo<T> implements Serializable {
      private static final long serialVersionUID = 1L;
      @NonNull String name;
      @NonNull ObservationExtractor<? super T> extractor;
      SerializableFunction<? super T, List<? extends T>> toSequence;
   }

   /**
    * Builder for creating {@link DataSetGenerator}
    *
    * @param <T> the raw type parameter
    */
   public static class Builder<T> {
      protected final List<GeneratorInfo<T>> generators = new ArrayList<>();
      @NonNull
      protected DataSetType dataSetType = DataSetType.InMemory;

      /**
       * Builds the DataSetGenerator
       *
       * @return the DataSetGenerator
       */
      public DataSetGenerator<T> build() {
         Validation.checkState(generators.size() > 0, "No extractors have been specified.");
         return new DataSetGenerator<>(dataSetType, generators);
      }

      /**
       * Sets the {@link DataSetType} of the generated {@link DataSet}
       *
       * @param dataSetType the data set type
       * @return this builder
       */
      public Builder<T> dataSetType(DataSetType dataSetType) {
         this.dataSetType = dataSetType;
         return this;
      }

      /**
       * Defines the default input ({@link Datum#DEFAULT_INPUT}) using the given extractor
       *
       * @param extractor  the extractor
       * @param toSequence the function to convert the raw input into a sequence of items.
       * @return the builder
       */
      public Builder<T> defaultInput(@NonNull ObservationExtractor<? super T> extractor,
                                     @NonNull SerializableFunction<? super T, List<? extends T>> toSequence) {
         generators.add(new GeneratorInfo<>(Datum.DEFAULT_INPUT, extractor, toSequence));
         return this;
      }

      /**
       * Defines the default input ({@link Datum#DEFAULT_INPUT}) using the given extractor
       *
       * @param extractor the extractor
       * @return the builder
       */
      public Builder<T> defaultInput(@NonNull ObservationExtractor<? super T> extractor) {
         generators.add(new GeneratorInfo<>(Datum.DEFAULT_INPUT, extractor, null));
         return this;
      }

      /**
       * Defines the default output ({@link Datum#DEFAULT_OUTPUT}) using the given extractor
       *
       * @param extractor  the extractor
       * @param toSequence the function to convert the raw input into a sequence of items.
       * @return the builder
       */
      public Builder<T> defaultOutput(@NonNull ObservationExtractor<? super T> extractor,
                                      @NonNull SerializableFunction<? super T, List<? extends T>> toSequence) {
         generators.add(new GeneratorInfo<>(Datum.DEFAULT_OUTPUT, extractor, toSequence));
         return this;
      }

      /**
       * Defines the default output ({@link Datum#DEFAULT_OUTPUT}) using the given extractor
       *
       * @param extractor the extractor
       * @return the builder
       */
      public Builder<T> defaultOutput(@NonNull ObservationExtractor<? super T> extractor) {
         generators.add(new GeneratorInfo<>(Datum.DEFAULT_OUTPUT, extractor, null));
         return this;
      }

      /**
       * Defines a new  observation source with the given name and extractor.
       *
       * @param name      the name
       * @param extractor the extractor
       * @return this builder
       */
      public Builder<T> source(@NonNull String name,
                               @NonNull ObservationExtractor<? super T> extractor) {
         generators.add(new GeneratorInfo<>(name, extractor, null));
         return this;
      }

      /**
       * Defines a new observation source with the given name and extractor.
       *
       * @param name       the name
       * @param extractor  the extractor
       * @param toSequence the function to convert the raw input into a sequence of items.
       * @return this builder
       */
      public Builder<T> source(@NonNull String name,
                               @NonNull ObservationExtractor<? super T> extractor,
                               @NonNull SerializableFunction<? super T, List<? extends T>> toSequence) {
         generators.add(new GeneratorInfo<>(name, extractor, toSequence));
         return this;
      }

   }//END OF Builder

}//END OF DataSetGenerator
