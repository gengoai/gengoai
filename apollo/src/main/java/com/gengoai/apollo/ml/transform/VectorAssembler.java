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

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Observation;
import lombok.NonNull;

import java.util.*;

/**
 * @author David B. Bracewell
 */
public class VectorAssembler implements Transform {
   private final List<String> inputs = new ArrayList<>(Collections.singleton(Datum.DEFAULT_INPUT));
   protected NDArrayFactory ndArrayFactory;
   private String output = Datum.DEFAULT_OUTPUT;

   public VectorAssembler() {
   }

   public VectorAssembler(@NonNull Collection<String> inputs, @NonNull String output) {
      this.inputs.clear();
      this.inputs.addAll(inputs);
      this.output = output;
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      return transform(dataset);
   }

   @Override
   public Set<String> getInputs() {
      return new HashSet<>(inputs);
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   public Transform inputs(@NonNull String... inputs) {
      return inputs(Arrays.asList(inputs));
   }

   public Transform inputs(@NonNull Collection<String> inputs) {
      Validation.checkArgument(inputs.size() > 0, "No sources specified");
      this.inputs.clear();
      this.inputs.addAll(inputs);
      return this;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      NDArray out = ndArrayFactory.array(inputs.size());
      for(int i = 0; i < inputs.size(); i++) {
         Observation o = datum.get(inputs.get(i));
         if(o.isVariable()) {
            out.set(i, o.asVariable().getValue());
         } else {
            out.set(i, o.asNDArray().scalar());
         }
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
