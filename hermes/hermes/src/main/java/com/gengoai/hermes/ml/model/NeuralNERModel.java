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
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.IOBValidator;
import com.gengoai.io.Resources;

import java.util.List;

import static com.gengoai.apollo.encoder.FixedEncoder.fixedEncoder;
import static com.gengoai.apollo.feature.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;
import static com.gengoai.hermes.ResourceType.WORD_LIST;
import static com.gengoai.tuple.Tuples.$;
import static java.util.stream.Collectors.toList;

public class NeuralNERModel extends TFSequenceLabeler {
   private static final long serialVersionUID = 1L;
   private static final String LABEL = "label";
   private static final String TOKENS = "words";
   private static final String CHARS = "chars";
   private static final String SHAPE = "shape";
   private static final int MAX_WORD_LENGTH = 10;

   public NeuralNERModel() {
      super(
            List.of(
                  TFInputVar.sequence(TOKENS, fixedEncoder(WORD_LIST.locate("glove", Language.ENGLISH).orElseThrow(),
                                                           "--UNKNOWN--")),
                  TFInputVar.sequence(SHAPE),
                  TFInputVar.sequence(CHARS, -1, MAX_WORD_LENGTH)
            ),
            List.of(
                  TFOutputVar.sequence(LABEL, "label/truediv", "O", IOBValidator.INSTANCE)
            ),
            IOB.decoder(Types.ML_ENTITY)
      );
   }

   public static void main(String[] args) throws Exception {
      Config.initialize("CNN", args, "com.gengoai.hermes");


      if (Math.random() < 0) {
         Config.setProperty("tfmodel.data", "/Users/ik/prj/gengoai/mono-repo/python/data/entity2.db");
         NeuralNERModel ner = new NeuralNERModel();
         DocumentCollection ontonotes = Corpus.open("/Volumes/ikdata-1/corpora/hermes_data/ontonotes_ner")
                                              .query("$SPLIT='TRAIN'");
         ner.estimate(ontonotes);
         ModelIO.save(ner, Resources.from("/Volumes/ikdata-1/hermes/en/models/ner-tmp"));
      } else {
//         NeuralNERModel2 ner = ModelIO.load(Resources.from("/Volumes/ikdata-1/hermes/en/models/ner-tmp/"));
//         DocumentCollection ontonotes = Corpus.open("/Users/ik/hermes_data/ontonotes_ner")
//                                              .query("$SPLIT='TEST'");
//         CoNLLEvaluation evaluation = new CoNLLEvaluation("label");
//         DataSet ds = ner.transform(ontonotes);
//         evaluation.evaluate(ner.delegate(), ds);
//         evaluation.report();

         Document document = Document.create("John Bowman sailed to the Virgin Islands last Saturday. " +
                                                   "He later went to Florida. " +
                                                   "He is married to the Stacey Abrhams of New Mexico.");
         document.annotate(Types.ENTITY);
         for (Annotation annotation : document.annotations(Types.ENTITY)) {
            System.out.println(annotation.toSGML(true));
         }
//
//         ElmoTokenEmbedding elmo = ResourceType.MODEL.load("elmo", Language.ENGLISH);
//         elmo.apply(document);
//         for (Annotation token : document.tokens()) {
//            System.out.println(token + ": " + token.embedding());
//         }
//
//         UniversalSentenceEncoder use = ResourceType.MODEL.load("sentence_encoder", Language.ENGLISH);
//         use.apply(document);
//         for (Annotation sentence : document.sentences()) {
//            System.out.println(sentence);
//            System.out.println(sentence.embedding());
//         }

      }
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(TOKENS, valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence(CHARS, booleanFeaturizer(h -> h.charNGrams(1)
                                                                                  .stream()
                                                                                  .map(HString::toLowerCase)
                                                                                  .collect(toList())))
                                    .tokenSequence(SHAPE, Features.WordShape)
                                    .source(LABEL, IOB.encoder(Types.ENTITY))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "1.2";
   }
}
