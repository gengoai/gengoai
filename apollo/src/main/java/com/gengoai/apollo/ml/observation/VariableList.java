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

import com.fasterxml.jackson.annotation.*;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <p>A {@link VariableCollection} backed by an ArrayList.</p>
 */
@JsonTypeName("vl")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class VariableList extends ArrayList<Variable> implements VariableCollection {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new VariableList.
    */
   public VariableList() {
      super(5);
   }

   /**
    * Instantiates a new VariableList.
    *
    * @param featureStream the feature stream
    */
   public VariableList(@NonNull Stream<Variable> featureStream) {
      featureStream.forEach(this::add);
   }

   /**
    * Instantiates a new VariableList.
    *
    * @param features the features
    */
   public VariableList(@NonNull Variable... features) {
      super(Arrays.asList(features));
   }

   /**
    * Instantiates a new VariableList.
    *
    * @param c the collection of features
    */
   @JsonCreator
   public VariableList(@JsonProperty("vars") @NonNull Collection<? extends Variable> c) {
      super(c);
   }

   @Override
   public VariableList copy() {
      return new VariableList(stream().map(Variable::copy));
   }

   @JsonProperty("vars")
   private List<Variable> getVariables() {
      return this;
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      for(int i = 0; i < size(); i++) {
         set(i, mapper.apply(get(i)));
      }
   }

}//END OF VariableList
