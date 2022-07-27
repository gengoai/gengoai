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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Sequence;
import com.gengoai.apollo.data.observation.VariableCollection;
import com.gengoai.apollo.data.observation.VariableNameSpace;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.compose.VectorCompositions;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * A transform that merges multiple non-NDArray {@link Observation}.
 * </p>
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class Merge implements Transform {
   private static final long serialVersionUID = 1L;
   @JsonProperty
   @Singular
   private final List<String> inputs;
   @JsonProperty
   private final String output ;
   @JsonProperty
   private final boolean prependSourceName;
   @JsonProperty
   private final boolean keepInputs;
   @JsonProperty
   private final VariableNameSpace nameSpace;


   @JsonCreator
   @Builder
   protected Merge(@JsonProperty("inputs") Collection<String> inputs,
                   @JsonProperty("output") String output,
                   @JsonProperty("prependSourceName") boolean prependSourceName,
                   @JsonProperty("keepInputs") boolean keepInputs,
                   @JsonProperty("nameSpace") VariableNameSpace nameSpace) {
      this.nameSpace = nameSpace;
      this.output = output;
      this.inputs = new ArrayList<>();
      if( inputs != null ){
         this.inputs.addAll(inputs);
      }
      this.keepInputs = keepInputs;
      this.prependSourceName = prependSourceName;
   }

   @Override
   public Merge copy() {
      return new Merge(inputs, output, prependSourceName, keepInputs, nameSpace);
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
      return Sets.asHashSet(inputs);
   }

   @Override
   @JsonIgnore
   public Set<String> getOutputs() {
      return Collections.singleton(Datum.DEFAULT_INPUT);
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      DataSet data = dataset.map(this::transform);
      final Class<? extends Observation> type = Cast.as(inputs.stream()
                                                              .map(s -> dataset.getMetadata(s).getType())
                                                              .filter(Objects::nonNull)
                                                              .findFirst()
                                                              .orElse(null));
      if (!keepInputs) {
         for (String source : inputs) {
            data.removeMetadata(source);
         }
      }
      data.updateMetadata(output, m -> {
         m.setEncoder(null);
         m.setDimension(-1);
         if (!Sequence.class.isAssignableFrom(type) && !NDArray.class.isAssignableFrom(type)) {
            m.setType(VariableCollection.class);
         } else {
            m.setType(type);
         }
      });
      return data;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      List<Observation> observations = new ArrayList<>();
      for (String input : inputs) {
         Observation o = datum.get(input);
         if (prependSourceName) {
            o = o.copy();
            o.updateVariables(v -> v.addSourceName(input));
         }
         observations.add(o);
      }
      if (observations.isEmpty()) {
         return datum;
      }
      assertCanMerge(observations);
      Observation out;
      if (observations.get(0).isNDArray()) {
         out = VectorCompositions.Sum.compose(Cast.cast(observations));
      } else if (observations.get(0).isSequence()) {
         out = Sequence.merge(Cast.cast(observations), nameSpace);
      } else {
         out = VariableCollection.mergeVariableSpace(observations.stream(), nameSpace);
      }
      if (!keepInputs) {
         inputs.forEach(datum::remove);
      }
      datum.put(output, out);
      return datum;
   }

   private void assertCanMerge(List<Observation> obs) {
      if (obs.stream().anyMatch(Sequence.class::isInstance)) {
         if (!obs.stream().allMatch(Sequence.class::isInstance)) {
            throw new IllegalStateException("Cannot merge non-sequences with sequences");
         }
      } else if (obs.stream().anyMatch(NDArray.class::isInstance)) {
         if (!obs.stream().allMatch(NDArray.class::isInstance)) {
            throw new IllegalStateException("Cannot merge non-NDArray with NDArray");
         }
         if (obs.stream().map(o -> o.asNDArray().shape()).distinct().count() > 1) {
            throw new IllegalStateException("Cannot merge NDArray of different shapes");
         }
      }
   }

   public static class MergeBuilder {
      private boolean isKeepInputs = true;
      private boolean isPrependSourceName = false;
      private VariableNameSpace nameSpace = VariableNameSpace.Full;
      private String output = Datum.DEFAULT_INPUT;

   }

}//END OF MergePreprocessor
