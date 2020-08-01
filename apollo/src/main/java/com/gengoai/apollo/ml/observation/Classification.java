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

package com.gengoai.apollo.ml.observation;

import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import lombok.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>
 * An unmodifiable labeling for classification-based models. Provides an easy association between labels and their
 * outcome in the classifier using a supplied {@link Encoder}. The raw {@link Model} output is obtainable via the {@link
 * #distribution()} method.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class Classification implements Serializable, Observation {
   private static final long serialVersionUID = 1L;
   private final String argMax;
   private final NDArray distribution;
   private Encoder encoder;

   /**
    * Instantiates a new Classification with an Encoder to facilitate label id to label mapping.
    *
    * @param distribution the distribution
    * @param encoder      the vectorizer
    */
   public Classification(@NonNull NDArray distribution, Encoder encoder) {
      if(distribution.shape().isScalar()) {
         this.distribution = NDArrayFactory.DENSE.array(1, 2);
         this.distribution.set(0, 1d - distribution.scalar());
         this.distribution.set(1, distribution.scalar());
      } else {
         this.distribution = distribution.shape().isColumnVector()
                             ? distribution.T()
                             : distribution.copy();
      }
      this.argMax = encoder != null
                    ? encoder.decode(this.distribution.argmax())
                    : Long.toString(this.distribution.argmax());
      this.encoder = encoder;
   }

   @Override
   public Classification asClassification() {
      return this;
   }

   /**
    * Gets the classification object as a Counter decoding the NDArray indexes into their label names and setting their
    * values to the associated index in the NDArray.
    *
    * @return the counter
    */
   public Counter<String> asCounter() {
      Validation.notNull(encoder, "No Encoder was provided");
      Counter<String> counter = Counters.newCounter();
      for(long i = 0; i < distribution.length(); i++) {
         counter.set(encoder.decode(i), distribution.get((int) i));
      }
      return counter;
   }

   @Override
   public NDArray asNDArray() {
      return distribution;
   }

   @Override
   public Classification copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Gets the underlying distribution of scores.
    *
    * @return the NDArray representing the distribution.
    */
   public NDArray distribution() {
      return distribution;
   }

   /**
    * Gets the argMax as a string either converting the id using the supplied Encoder or using
    * <code>Integer.toString</code>
    *
    * @return the result
    */
   public String getResult() {
      return argMax;
   }

   /**
    * Gets the score for a given label.
    *
    * @param label the label
    * @return the score (e.g. probability)
    */
   public double getScore(@NonNull String label) {
      Validation.notNull(encoder, "No Encoder was provided");
      return distribution.get(encoder.encode(label));
   }

   @Override
   public Stream<Variable> getVariableSpace() {
      return Stream.empty();
   }

   @Override
   public boolean isClassification() {
      return true;
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      throw new UnsupportedOperationException("Classification does not support mapping.");
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      throw new UnsupportedOperationException("Classification does not support filtering.");
   }

   @Override
   public String toString() {
      if(encoder == null) {
         return "Classification{" + distribution + "}";
      }
      return "Classification{" + asCounter() + "}";

   }

   @Override
   public void updateVariables(@NonNull Consumer<Variable> updater) {
      throw new UnsupportedOperationException("Classification does not support updating.");
   }
}//END OF Classification
