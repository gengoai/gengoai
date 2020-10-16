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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.NoOptEncoder;
import com.gengoai.apollo.ml.model.*;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Maps;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

public class ElmoTokenEmbedding extends TensorFlowModel implements HStringMLModel, ContextualizedEmbedding {
   public static final int DIMENSION = 1024;
   public static final String TOKENS = "tokens";
   public static final String SEQUENCE_LENGTH = "sequence_len";
   public static final String OUTPUT = "output";
   private static final long serialVersionUID = 1L;

   public ElmoTokenEmbedding() {
      super(Map.of(Datum.DEFAULT_INPUT, TFVarSpec.varSpec(TOKENS, NoOptEncoder.INSTANCE, -1)),
            Maps.linkedHashMapOf($(Datum.DEFAULT_INPUT, TFVarSpec.varSpec(OUTPUT, NoOptEncoder.INSTANCE, -1))));
   }

   @Override
   public HString apply(@NonNull HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      Iterators.zip(hString.sentences().iterator(), processBatch(dataSet).iterator())
               .forEachRemaining(e -> {
                  Annotation sentence = e.getKey();
                  NDArray embeddings = e.getValue().getDefaultInput().asNDArray();
                  for (int i = 0; i < sentence.tokenLength(); i++) {
                     sentence.tokenAt(i).put(Types.EMBEDDING, embeddings.getRow(i));
                  }
               });
      return hString;
   }

   @Override
   protected Map<String, Tensor<?>> createTensors(DataSet batch) {
      return TensorUtils.sequence2StringTensor(batch, Datum.DEFAULT_INPUT, TOKENS, SEQUENCE_LENGTH);
   }

   @Override
   protected Observation decodeNDArray(String name, NDArray ndArray) {
      return ndArray;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(Datum.DEFAULT_INPUT, h -> Variable.binary(h.toString()))
                                    .build();
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(getOutput())) {
         return LabelType.NDArray;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model");
   }

   @Override
   public String getVersion() {
      return "https://tfhub.dev/google/elmo/3";
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }


}//END OF UniversalSentenceEncoder
