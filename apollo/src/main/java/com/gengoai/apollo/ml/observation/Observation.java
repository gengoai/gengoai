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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Copyable;
import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.SparseMatrix;
import com.gengoai.apollo.math.linalg.Tensor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>
 * An observation represents a single data point derived from an object which describes an <code>input</code> or
 * <code>output</code> for a model. Observations are made up of one or more variables, which are aspects of the object
 * from which the observation is derived. All variables in Apollo are continuous having a name and value.
 * </p>
 * <p>
 * Categorical variables are represented with their category as the variable name and a <code>1</code> as the value. For
 * example, given a Categorical variable <code>HomeType</code> made up of the values  <code>Ranch</code>,
 * <code>Colonial</code>, and <code>Tudor</code>, we can represent an instance of <code>Ranch</code> as a continuous
 * variable <code>Ranch=1</code>.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
      {
            @JsonSubTypes.Type(value = Variable.class, name = "v"),
            @JsonSubTypes.Type(value = VariableList.class, name = "vl"),
            @JsonSubTypes.Type(value = VariableCollectionSequence.class, name = "vcs"),
            @JsonSubTypes.Type(value = VariableSequence.class, name = "vs"),
            @JsonSubTypes.Type(value = DenseMatrix.class, name = "dm"),
            @JsonSubTypes.Type(value = SparseMatrix.class, name = "sm"),
            @JsonSubTypes.Type(value = Tensor.class, name = "tensor"),
      }
)
public interface Observation extends Copyable<Observation>, Serializable {

   /**
    * Casts this Observation as a {@link Classification} throwing  {@link IllegalArgumentException} if the cast is
    * invalid.
    *
    * @return the classification
    */
   default Classification asClassification() {
      throw new IllegalArgumentException(getClass().getSimpleName() + " is not a Classification.");
   }

   /**
    * Casts this Observation as a {@link NDArray} throwing  {@link IllegalArgumentException} if the cast is invalid.
    *
    * @return the nd array
    */
   default NDArray asNDArray() {
      throw new IllegalArgumentException(getClass().getSimpleName() + " is not an NDArray.");
   }

   /**
    * Casts this Observation as a {@link Sequence} throwing  {@link IllegalArgumentException} if the cast is invalid.
    *
    * @return the sequence
    */
   default Sequence<? extends Observation> asSequence() {
      throw new IllegalArgumentException(getClass().getSimpleName() + " is not a Sequence.");
   }

   /**
    * Casts this Observation as a {@link Variable} throwing  {@link IllegalArgumentException} if the cast is invalid.
    *
    * @return the variable
    */
   default Variable asVariable() {
      throw new IllegalArgumentException(getClass().getSimpleName() + " is not a Variable.");
   }

   /**
    * Casts this Observation as a {@link VariableCollection} throwing  {@link IllegalArgumentException} if the cast is
    * invalid.
    *
    * @return the variable collection
    */
   default VariableCollection asVariableCollection() {
      throw new IllegalArgumentException(getClass().getSimpleName() + " is not a VariableCollection.");
   }

   /**
    * Gets a stream of all of the variables making up this observation and any sub-observations (e.g. {@link
    * VariableCollectionSequence )}.
    *
    * @return the variable space
    */
   Stream<Variable> getVariableSpace();

   /**
    * Type check for {@link Classification}
    *
    * @return True - if this observation is an instance of a Classification, False otherwise.
    */
   default boolean isClassification() {
      return false;
   }

   /**
    * Type check for {@link NDArray}
    *
    * @return True - if this observation is an instance of an NDArray, False otherwise.
    */
   default boolean isNDArray() {
      return false;
   }

   /**
    * Type check for {@link Sequence}
    *
    * @return True - if this observation is an instance of a Sequence, False otherwise.
    */
   default boolean isSequence() {
      return false;
   }

   /**
    * Type check for {@link Variable}
    *
    * @return True - if this observation is an instance of a Variable, False otherwise.
    */
   default boolean isVariable() {
      return false;
   }

   /**
    * Type check for {@link VariableCollection}
    *
    * @return True - if this observation is an instance of a VariableCollection, False otherwise.
    */
   default boolean isVariableCollection() {
      return false;
   }

   /**
    * Maps the variables in this Observation to new values
    *
    * @param mapper the mapper
    */
   void mapVariables(@NonNull Function<Variable, Variable> mapper);

   /**
    * Filters out, i.e. removes, the variables evaluating to true for the given filter.
    *
    * @param filter the filter
    */
   void removeVariables(@NonNull Predicate<Variable> filter);

   /**
    * Updates the variables in this Observation
    *
    * @param updater the updater
    */
   void updateVariables(@NonNull Consumer<Variable> updater);

}//END OF Observation
