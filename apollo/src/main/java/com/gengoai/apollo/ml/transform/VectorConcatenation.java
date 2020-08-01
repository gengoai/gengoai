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

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.collection.Sets;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * <p>
 * Concatenates vectors from the given inputs.
 * </p>
 *
 * @author David B. Bracewell
 */
public class VectorConcatenation implements Transform {
   private static final long serialVersionUID = 1L;
   private final LinkedHashSet<String> inputs = Sets.linkedHashSetOf(Datum.DEFAULT_INPUT);
   protected NDArrayFactory factory;
   private String output = Datum.DEFAULT_INPUT;
   @Getter
   @Setter
   @Accessors(fluent = true)
   private boolean dropInputs = true;

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      return transform(dataset);
   }

   @Override
   public Set<String> getInputs() {
      return Collections.unmodifiableSet(inputs);
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   /**
    * Sets the inputs to concatenate (order matters)
    *
    * @param inputs the inputs
    * @return this VectorConcatenation
    */
   public VectorConcatenation inputs(@NonNull String... inputs) {
      return inputs(Arrays.asList(inputs));
   }

   /**
    * Sets the inputs to concatenate (order matters)
    *
    * @param inputs the inputs
    * @return this VectorConcatenation
    */
   public VectorConcatenation inputs(@NonNull List<String> inputs) {
      this.inputs.clear();
      this.inputs.addAll(inputs);
      return this;
   }

   /**
    * Sets the output source to put the concatenated NDArray
    *
    * @param output the output
    * @return this VectorConcatenation
    */
   public VectorConcatenation output(@NonNull String output) {
      this.output = output;
      return this;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      List<NDArray> l = new ArrayList<>();
      for(String input : inputs) {
         l.add(datum.get(input).asNDArray());
      }
      datum.put(output, factory.hstack(l));
      if(dropInputs) {
         inputs.stream().filter(i -> !output.equals(i)).forEach(datum::remove);
      }
      return datum;
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      factory = dataset.getNDArrayFactory();
      dataset = dataset.map(this::transform);
      int dimension = (int) dataset.getMetadata().entrySet()
                                   .stream()
                                   .filter(e -> inputs.contains(e.getKey()))
                                   .mapToLong(e -> e.getValue().getDimension())
                                   .sum();
      if(dropInputs) {
         inputs.forEach(dataset::removeMetadata);
      }
      dataset.updateMetadata(output, m -> {
         m.setDimension(dimension);
         m.setType(NDArray.class);
         m.setEncoder(null);
      });
      return dataset;
   }
}//END OF VectorConcat
