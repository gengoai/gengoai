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

package com.gengoai.apollo.ml.model.topic;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collection;

import static com.gengoai.apollo.ml.observation.VariableCollection.mergeVariableSpace;

/**
 * <p>Abstract base class for {@link TopicModel}s backed by {@link NDArray}</p>
 *
 * @author David B. Bracewell
 */
public abstract class BaseVectorTopicModel extends TopicModel {
   protected final IndexEncoder encoder = new IndexEncoder();

   /**
    * Fits the given {@link Encoder} to the given {@link DataSet} for the given sources.
    *
    * @param dataset   the dataset
    * @param sources   the sources
    * @param nameSpace the {@link VariableNameSpace} to use when fitting the dataset
    */
   protected void encoderFit(@NonNull DataSet dataset,
                             @NonNull Collection<String> sources,
                             @NonNull VariableNameSpace nameSpace) {
      encoder.fit(dataset.stream()
                         .flatMap(d -> d.stream(sources))
                         .flatMap(Observation::getVariableSpace)
                         .map(nameSpace::transform));
   }

   /**
    * perform inference on the given document represented as an {@link NDArray}
    *
    * @param n the document
    * @return the topic distribution
    */
   protected abstract NDArray inference(NDArray n);

   /**
    * Encodes the variable space of the given {@link Observation} into an {@link NDArray} using the given encoder where
    * the value an index in the array is equal to the sum of the values of all occurrences of the associated {@link
    * Variable}*. The resulting array has a shape of <code>1 x Encoder.size()</code>
    *
    * @param observation the observation
    * @param nameSpace   the {@link VariableNameSpace} to use when encoding.
    * @return the NDArray
    */
   protected NDArray toCountVector(@NonNull Observation observation,
                                   @NonNull VariableNameSpace nameSpace) {
      NDArray n = NDArrayFactory.ND.array(encoder.size());
      observation.getVariableSpace()
                 .forEach(v -> {
                    int index = encoder.encode(nameSpace.getName(v));
                    if(index >= 0) {
                       n.set(index, n.get(index) + v.getValue());
                    }
                 });
      return n;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      if(getFitParameters().combineOutputs.value()) {
         datum.put(getFitParameters().output.value(),
                   inference(toCountVector(mergeVariableSpace(datum.stream(getFitParameters().inputs.value())),
                                           getFitParameters().namingPattern.value())));
      } else {
         for(String input : getFitParameters().inputs.value()) {
            datum.put(input + Strings.nullToEmpty(getFitParameters().outputSuffix.value()),
                      inference(toCountVector(datum.get(input),
                                              getFitParameters().namingPattern.value())));
         }
      }
      return datum;
   }

}//END OF BaseVectorTopicModel
