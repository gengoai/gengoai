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
import com.gengoai.apollo.math.linalg.VectorCompositions;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.VariableCollection;
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.*;

/**
 * <p>
 * A transform that merges multiple non-NDArray {@link Observation}.
 * </p>
 */
@Builder
public class Merge implements Transform {
   private static final long serialVersionUID = 1L;
   @Singular
   private final List<String> inputs;
   private final String output;
   private final boolean prependSourceName;
   private final boolean keepInputs;
   private final VariableNameSpace nameSpace;

   /**
    * Instantiates a new Merge transform.
    *
    * @param inputs            the names of the observation sources to merge
    * @param output            the name of the source to output the merge to
    * @param prependSourceName True - prepends the source name to Variables in each observation, False - keep Variable
    *                          names as is.
    * @param keepInputs        True - keep the input sources, False - remove the input sources from the Datum.
    * @param nameSpace         the {@link VariableNameSpace} to use when merging.
    */
   public Merge(@NonNull List<String> inputs,
                @NonNull String output,
                boolean prependSourceName,
                boolean keepInputs, VariableNameSpace nameSpace) {
      this.nameSpace = nameSpace;
      Validation.checkArgument(inputs.size() >= 2, "Must specify two or more sources to merge.");
      this.output = output;
      this.inputs = new ArrayList<>(inputs);
      this.keepInputs = keepInputs;
      this.prependSourceName = prependSourceName;
   }

   private void assertCanMerge(List<Observation> obs) {
      if(obs.stream().anyMatch(Sequence.class::isInstance)) {
         if(!obs.stream().allMatch(Sequence.class::isInstance)) {
            throw new IllegalStateException("Cannot merge non-sequences with sequences");
         }
      } else if(obs.stream().anyMatch(NDArray.class::isInstance)) {
         if(!obs.stream().allMatch(NDArray.class::isInstance)) {
            throw new IllegalStateException("Cannot merge non-NDArray with NDArray");
         }
         if(obs.stream().map(o -> o.asNDArray().shape()).distinct().count() > 1) {
            throw new IllegalStateException("Cannot merge NDArray of different shapes");
         }
      }
   }

   @Override
   public Merge copy() {
      return new Merge(inputs, output, prependSourceName, keepInputs, nameSpace);
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      return transform(dataset);
   }

   @Override
   public Set<String> getInputs() {
      return Sets.asHashSet(inputs);
   }

   @Override
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
      if(!keepInputs) {
         for(String source : inputs) {
            data.removeMetadata(source);
         }
      }
      data.updateMetadata(output, m -> {
         m.setEncoder(null);
         m.setDimension(-1);
         if(!Sequence.class.isAssignableFrom(type) && !NDArray.class.isAssignableFrom(type)) {
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
      for(String input : inputs) {
         Observation o = datum.get(input);
         if(prependSourceName) {
            o = o.copy();
            o.updateVariables(v -> v.addSourceName(input));
         }
         observations.add(o);
      }
      if(observations.isEmpty()) {
         return datum;
      }
      assertCanMerge(observations);
      Observation out;
      if(observations.get(0).isNDArray()) {
         out = VectorCompositions.Sum.compose(Cast.cast(observations));
      } else if(observations.get(0).isSequence()) {
         out = Sequence.merge(Cast.cast(observations), nameSpace);
      } else {
         out = VariableCollection.mergeVariableSpace(observations.stream(), nameSpace);
      }
      if(!keepInputs) {
         inputs.forEach(datum::remove);
      }
      datum.put(output, out);
      return datum;
   }

   public static class MergeBuilder {
      private boolean isKeepInputs = true;
      private boolean isPrependSourceName = false;
      private VariableNameSpace nameSpace = VariableNameSpace.Full;
   }

}//END OF MergePreprocessor
