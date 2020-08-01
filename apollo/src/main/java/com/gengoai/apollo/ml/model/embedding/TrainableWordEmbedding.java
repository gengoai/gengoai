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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.CombinableOutputModel;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * A base class for {@link WordEmbedding}s that can be learned from a {@link com.gengoai.apollo.ml.DataSet}.
 * </p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class TrainableWordEmbedding<F extends WordEmbeddingFitParameters<F>, T extends TrainableWordEmbedding<F, T>>
      extends WordEmbedding implements CombinableOutputModel<F, TrainableWordEmbedding<F, T>> {

   private static final long serialVersionUID = 1L;
   protected final F parameters;

   /**
    * Instantiates a new TrainableWordEmbedding.
    *
    * @param parameters the parameters
    */
   protected TrainableWordEmbedding(@NonNull F parameters) {
      this.parameters = parameters;
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      estimate(dataset);
      return transform(dataset);
   }

   @Override
   public F getFitParameters() {
      return parameters;
   }

   @Override
   public Set<String> getInputs() {
      return parameters.inputs.value();
   }

   @Override
   public LabelType getOutputType() {
      return LabelType.NDArray;
   }

   @Override
   protected String getVariableName(Variable v) {
      return parameters.nameSpace.value().getName(v);
   }

   /**
    * Sets the input sources to the MultiInputTransform
    *
    * @param inputs the inputs
    * @return this MultiInputTransform
    */
   public T inputs(@NonNull String... inputs) {
      Validation.checkArgument(inputs.length > 0, "Must specify at least one input");
      parameters.inputs.set(Set.of(inputs));
      return Cast.as(this);
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      if(parameters.combineOutputs.value()) {
         if(parameters.inputs.value().size() > 1) {
            datum.put(parameters.output.value(),
                      parameters.aggregationFunction.value().compose(datum.stream(getInputs())
                                                                          .map(this::transform)
                                                                          .collect(Collectors.toList())));
         } else {
            datum.put(parameters.output.value(), transform(datum.get(Iterables.getFirst(getInputs(), null))));
         }
      } else {
         for(String source : getInputs()) {
            datum.put(source + parameters.outputSuffix.value(), transform(datum.get(source)));
         }
      }
      return datum;
   }
}//END OF Embedding
