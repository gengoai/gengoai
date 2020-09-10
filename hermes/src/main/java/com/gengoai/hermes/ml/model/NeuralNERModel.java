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

import com.gengoai.Language;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.feature.Featurizer;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Maps;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.*;
import com.gengoai.hermes.ml.feature.Features;
import org.tensorflow.Tensor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class NeuralNERModel extends TensorFlowSequenceLabeler implements HStringMLModel {
   public static final String LABEL = "label";
   public static final String TOKENS = "words";
   public static final String CHARS = "chars";
   public static final String SHAPE = "shape";
   private static final int MAX_WORD_LENGTH = 10;

   protected NeuralNERModel() {
      super(Set.of(TOKENS, CHARS, SHAPE),
            Maps.linkedHashMapOf($(LABEL, "label/truediv")),
            Map.of(
                  TOKENS, new FixedEncoder(ResourceType.WORD_LIST.locate("glove", Language.ENGLISH)
                                                                 .orElseThrow(), "--UNKNOWN--"),
                  CHARS, new IndexEncoder("-PAD-"),
                  SHAPE, new IndexEncoder("-PAD-"),
                  LABEL, new IndexEncoder("O")
            ),
            IOBValidator.INSTANCE,
            IOB.decoder(Types.ML_ENTITY));
   }

//   public static void main(String[] args) throws Exception {
//      Config.initialize("CNN", args, "com.gengoai.hermes");
//
//      if (Math.random() < 0) {
//         CNNNer ner = new CNNNer();
//         SearchResults ontonotes = Corpus.open("/data/corpora/hermes_data/ontonotes_ner")
//                                         .query("$SPLIT='TRAIN'");
//         ner.estimate(ontonotes);
//         ModelIO.save(ner, Resources.from("/data/hermes/en/models/ner-cnn"));
//      } else {
//         CNNNer ner = ModelIO.load(Resources.from("/data/hermes/en/models/ner-cnn"));
//         DocumentCollection ontonotes = Corpus.open("/data/corpora/hermes_data/ontonotes_ner")
//                                              .query("$SPLIT='TEST'");
//         CoNLLEvaluation evaluation = new CoNLLEvaluation("label");
//         evaluation.evaluate(ner.delegate(), ner.transform(ontonotes));
//         evaluation.report();
//      }
//
//
//   }

   @Override
   protected Map<String, Tensor<?>> createTensors(DataSet dataSet) {
      int max_length = (int) dataSet.stream()
                                    .mapToDouble(d -> d.get(TOKENS).asNDArray().rows())
                                    .max().orElse(0d);
      int index = 0;
      NDArray words = NDArrayFactory.ND.array((int) dataSet.size(), max_length);
      NDArray shape = NDArrayFactory.ND.array((int) dataSet.size(), max_length);
      NDArray chars = NDArrayFactory.ND.array((int) dataSet.size(), max_length, MAX_WORD_LENGTH);
      for (Datum datum : dataSet) {
         words.setRow(index, datum.get(TOKENS).asNDArray().padRowPost(max_length).T());
         shape.setRow(index, datum.get(SHAPE).asNDArray().padRowPost(max_length).T());
         NDArray c = datum.get(CHARS).asNDArray().padPost(max_length, MAX_WORD_LENGTH);
         chars.setSlice(index, c);
         index++;
      }
      return Map.of(
            TOKENS, Tensor.create(words.toFloatArray2()),
            CHARS, Tensor.create(chars.toFloatArray3()),
            SHAPE, Tensor.create(shape.toFloatArray2())
      );
   }

   @Override
   protected Transformer createTransformer() {
      var tokens = new IndexingVectorizer(encoders.get(TOKENS)).source(TOKENS);
      var chars = new IndexingVectorizer(encoders.get(CHARS)).source(CHARS);
      var shape = new IndexingVectorizer(encoders.get(SHAPE)).source(SHAPE);
      var label = new IndexingVectorizer(encoders.get(LABEL)).source(LABEL);
      return new Transformer(List.of(tokens, chars, shape, label));
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(TOKENS, Featurizer.valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence(CHARS,
                                                   Featurizer.booleanFeaturizer(h -> h.charNGrams(1)
                                                                                      .stream()
                                                                                      .map(HString::toLowerCase)
                                                                                      .collect(Collectors.toList())))
                                    .tokenSequence(SHAPE, Features.WordShape)
                                    .source(LABEL, IOB.encoder(Types.ENTITY))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "1.1";
   }

}//END OF CNNNer
