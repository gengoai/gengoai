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

package com.gengoai.hermes.ml.model;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.collection.Iterators;
import com.gengoai.hermes.*;
import com.gengoai.hermes.ml.ContextualizedEmbedding;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.HStringMLModel;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public class UniversalSentenceEncoder extends TFModel implements HStringMLModel, ContextualizedEmbedding {
   public static final int DIMENSION = 512;
   private static final long serialVersionUID = 1L;

   public UniversalSentenceEncoder() {
      super(
            List.of(TFInputVar.sequence(Datum.DEFAULT_INPUT, NoOptEncoder.INSTANCE)),
            List.of(TFOutputVar.embedding(Datum.DEFAULT_OUTPUT, Datum.DEFAULT_OUTPUT))
      );
   }

   @Override
   public HString apply(@NonNull HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      Iterators.zip(hString.sentences().iterator(), processBatch(dataSet).iterator())
               .forEachRemaining(e -> {
                  Annotation sentence = e.getKey();
                  NumericNDArray embeddings = e.getValue().getDefaultOutput().asNumericNDArray();
                  sentence.put(Types.EMBEDDING, embeddings);
               });
      return hString;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .defaultInput(h -> nd.DSTRING.scalar(h.toString()))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "https://tfhub.dev/google/universal-sentence-encoder-large/3";
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }

}//END OF UniversalSentenceEncoder
