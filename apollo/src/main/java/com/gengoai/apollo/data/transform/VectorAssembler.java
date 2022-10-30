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

package com.gengoai.apollo.data.transform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Validation;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Assemblies multiple Variables or Scalar NDArray into a single NDArray. By default it will assemble all
 * observations on the datum and store the result into <code>Datum.DEFAULT_INPUT</code>. This can be modified by setting
 * the inputs and output of the transform.</p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor
public class VectorAssembler implements Transform {
   @JsonProperty
   private final List<String> inputs = new ArrayList<>();
   @JsonProperty
   private NDArrayFactory ndArrayFactory;
   @JsonProperty
   private String output = Datum.DEFAULT_INPUT;
   @Getter
   @Setter
   private boolean keepInputs = true;

   /**
    * Instantiates a new Vector assembler.
    *
    * @param inputs the inputs
    * @param output the output
    */
   public VectorAssembler(@NonNull Collection<String> inputs, @NonNull String output) {
      this.inputs(inputs);
      this.output(output);
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      if (inputs.isEmpty()) {
         inputs.addAll(dataset.parallelStream()
                              .flatMap(d -> d.keySet().stream())
                              .distinct()
                              .collect(Collectors.toList()));
      }
      return transform(dataset);
   }

   @Override
   @JsonIgnore
   public Set<String> getInputs() {
      return new HashSet<>(inputs);
   }

   @Override
   @JsonIgnore
   public Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   /**
    * Inputs transform.
    *
    * @param inputs the inputs
    * @return the transform
    */
   public Transform inputs(@NonNull String... inputs) {
      return inputs(Arrays.asList(inputs));
   }

   /**
    * Inputs transform.
    *
    * @param inputs the inputs
    * @return the transform
    */
   public Transform inputs(@NonNull Collection<String> inputs) {
      this.inputs.clear();
      this.inputs.addAll(inputs);
      return this;
   }

   /**
    * Output transform.
    *
    * @param outputName the output name
    * @return the transform
    */
   public Transform output(@NonNull String outputName) {
      this.output = Validation.notNullOrBlank(outputName);
      return this;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      NumericNDArray out = ndArrayFactory.asNumeric().zeros(inputs.size());
      for (int i = 0; i < inputs.size(); i++) {
         Observation o = datum.get(inputs.get(i));
         if (o.isVariable()) {
            out.set(i, o.asVariable().getValue());
         } else {
            out.set(i, o.asNDArray().scalar());
         }
      }
      if( !keepInputs ){
         inputs.forEach(datum::remove);
      }
      datum.put(output, out);
      return datum;
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      ndArrayFactory = dataset.getNDArrayFactory();
      dataset = dataset.map(this::transform);
      dataset.updateMetadata(output, m -> {
         m.setDimension(inputs.size());
         m.setEncoder(null);
         m.setType(NDArray.class);
      });
      return dataset;
   }

}//END OF VectorAssembler
