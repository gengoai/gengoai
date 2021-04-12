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

package com.gengoai.hermes.ml.model.embedding;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.collection.Iterators;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.ContextualizedEmbedding;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.HStringMLModel;

import java.util.Collections;
import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class ElmoTokenEmbedding extends TFModel implements HStringMLModel, ContextualizedEmbedding {
   public static final int DIMENSION = 1024;
   public static final String TOKENS = "tokens";
   public static final String SEQUENCE_LENGTH = "sequence_len";
   public static final String OUTPUT = "output";
   private static final long serialVersionUID = 1L;

   public ElmoTokenEmbedding() {
      super(
            List.of(
                  TFInputVar.sequence(TOKENS, NoOptEncoder.INSTANCE, -1),
                  TFInputVar.var(SEQUENCE_LENGTH, SEQUENCE_LENGTH, NoOptEncoder.INSTANCE)
            ),
            List.of(
                  TFOutputVar.embedding(Datum.DEFAULT_INPUT, OUTPUT)
            ));
   }

   @Override
   public HString apply(HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      Iterators.zip(hString.sentences().iterator(), processBatch(dataSet).iterator())
               .forEachRemaining(e -> {
                  Annotation sentence = e.getKey();
                  NumericNDArray embeddings = e.getValue().getDefaultInput().asNumericNDArray();
                  for (int i = 0; i < sentence.tokenLength(); i++) {
                     sentence.tokenAt(i).put(Types.EMBEDDING, embeddings.getAxis(Shape.ROW, i));
                  }
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
                                    .source(TOKENS, h -> {
                                       int tokenLength = h.tokenLength();
                                       String[] tokenStrs = h.tokens()
                                                             .stream()
                                                             .map(Annotation::toString)
                                                             .toArray(String[]::new);
                                       return nd.DSTRING.array(Shape.shape(tokenLength, 1), tokenStrs);
                                    })
                                    .source(SEQUENCE_LENGTH,
                                            h -> nd.DINT32.scalar(h.tokenLength()))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "https://tfhub.dev/google/elmo/3";
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }
}//END OF ElmoTokenEmbedding2
