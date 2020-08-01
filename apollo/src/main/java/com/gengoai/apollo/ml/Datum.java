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

import com.gengoai.Copyable;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.gengoai.collection.Sets.asHashSet;
import static com.gengoai.collection.Sets.hashSetOf;

/**
 * <p>
 * A datum represents a <b>mutable</b> set of {@link Observation}s that make up the input and output data for a model.
 * Datum support having multiple named observations. For convenience, there are a set of methods for manipulating a
 * default input and output name defined using the static constants <code>DEFAULT_INPUT</code> and
 * <code>DEFAULT_OUTPUT</code>.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
public final class Datum extends HashMap<String, Observation> implements Serializable, Copyable<Datum> {
   /**
    * Name associated with a default input observation
    */
   public static final String DEFAULT_INPUT = "input";
   /**
    * Name associated with a default output observation
    */
   public static final String DEFAULT_OUTPUT = "output";

   /**
    * Construct a Datum from the given observations defined as <code>name</code> and <code>value</code> tuples.
    *
    * @param observations the observations and their names
    * @return the datum
    */
   @SafeVarargs
   public static Datum of(@NonNull Tuple2<String, ? extends Observation>... observations) {
      Datum datum = new Datum(observations.length);
      for(Tuple2<String, ? extends Observation> observation : observations) {
         datum.put(observation.v1, observation.v2);
      }
      return datum;
   }

   /**
    * Instantiates a new Datum.
    *
    * @param expectedNumberOfObservations the expected number of observations
    */
   public Datum(int expectedNumberOfObservations) {
      super(expectedNumberOfObservations);
   }

   /**
    * Instantiates a new Datum.
    */
   public Datum() {
      super(2);
   }

   /**
    * Instantiates a new Datum from the given mapping of names to observations.
    *
    * @param observations the observations to add
    */
   public Datum(@NonNull Map<String, Observation> observations) {
      putAll(observations);
   }

   @Override
   public Datum copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Gets the Observation associated with <code>DEFAULT_INPUT</code>.
    *
    * @return the Observation
    */
   public Observation getDefaultInput() {
      return get(DEFAULT_INPUT);
   }

   /**
    * Gets the Observation associated with <code>DEFAULT_OUTPUT</code>.
    *
    * @return the Observation
    */
   public Observation getDefaultOutput() {
      return get(DEFAULT_OUTPUT);
   }

   @Override
   public Observation put(@NonNull String name, Observation observation) {
      if(observation == null) {
         return remove(name);
      }
      return super.put(name, observation);
   }

   /**
    * Sets the observation associated with the <code>DEFAULT_INPUT</code>.
    *
    * @param observation the observation
    * @return The old value for <code>DEFAULT_INPUT</code>
    */
   public Observation setDefaultInput(Observation observation) {
      return put(DEFAULT_INPUT, observation);
   }

   /**
    * Sets the observation associated with the <code>DEFAULT_OUTPUT</code>.
    *
    * @param observation the observation
    * @return The old value for <code>DEFAULT_OUTPUT</code>
    */
   public Observation setDefaultOutput(Observation observation) {
      return put(DEFAULT_OUTPUT, observation);
   }

   /**
    * Generates a stream of {@link Observation} over the given sources.
    *
    * @param sources the sources whose Observations we want
    * @return the stream of Observations
    */
   public Stream<Observation> stream(@NonNull String... sources) {
      final Set<String> target = hashSetOf(sources);
      return entrySet().stream().filter(e -> target.contains(e.getKey())).map(Map.Entry::getValue);
   }

   /**
    * Generates a stream of {@link Observation} over the given sources.
    *
    * @param sources the sources whose Observations we want
    * @return the stream of Observations
    */
   public Stream<Observation> stream(@NonNull Collection<String> sources) {
      final Set<String> target = asHashSet(sources);
      return entrySet().stream().filter(e -> target.contains(e.getKey())).map(Map.Entry::getValue);
   }

   @Override
   public String toString() {
      return "Datum[" + Strings.join(keySet(), ", ") + "]";
   }

   /**
    * Updates the {@link Observation} with the given name using the given transform function.
    *
    * @param name     the name of the observation
    * @param function the function to transforn the observation
    * @return the datum with the given named observation updated.
    */
   public Datum update(@NonNull String name, @NonNull Function<Observation, Observation> function) {
      compute(name, (k, v) -> function.apply(v));
      return this;
   }

}//END OF Datum
