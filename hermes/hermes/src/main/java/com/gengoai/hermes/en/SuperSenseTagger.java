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

package com.gengoai.hermes.en;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.model.Consts;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOneHotInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.CoNLLEvaluation;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.IOBValidator;
import com.gengoai.hermes.ml.model.TFSequenceLabeler;
import com.gengoai.io.Resources;

import java.util.List;

import static com.gengoai.apollo.encoder.FixedEncoder.fixedEncoder;
import static com.gengoai.apollo.feature.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;
import static java.util.stream.Collectors.toList;

public class SuperSenseTagger extends TFSequenceLabeler {
   private static final long serialVersionUID = 1L;
   private static final String LABEL = "label";
   private static final String TOKENS = "words";
   private static final String CHARS = "chars";
   private static final String POS = "pos";
   private static final String CHUNK = "chunk";
   private static final int MAX_WORD_LENGTH = 25;

   public SuperSenseTagger() {
      super(
            List.of(
                  TFInputVar.sequence(TOKENS, fixedEncoder(ENResources.gloveSmallLexicon(), Consts.UNKNOWN_WORD)),
                  TFInputVar.sequence(CHARS, -1, MAX_WORD_LENGTH),
                  TFInputVar.oneHotEncoding(POS, -1, 17)
                   ),
            List.of(
                  TFOutputVar.sequence(LABEL,
                                       "label/truediv",
                                       "O", IOBValidator.INSTANCE)
                   ),
            IOB.decoder(Types.SUPER_SENSE)
           );
   }

   public static void main(String[] args) throws Exception {
      Config.initialize("CNN", args, "com.gengoai.hermes");
      if (Math.random() < 0) {
         Config.setProperty("tfmodel.data", "/work/prj/gengoai/python/tensorflow/data/supersense.db");
         SuperSenseTagger ner = new SuperSenseTagger();
         DocumentCollection ontonotes = DocumentCollection.create("conll::/home/ik/Downloads/streusle_train.txt;fields=WORD,SUPER_SENSE;docPerSentence=True")
                                                          .annotate(Types.PART_OF_SPEECH, Types.PHRASE_CHUNK).cache();
         ner.estimate(ontonotes);
         ModelIO.save(ner, Resources.from("/home/ik/hermes/en/models/supersense/"));
      } else {
         SuperSenseTagger ner = ModelIO.load(Resources.from("/home/ik/hermes/en/models/supersense/"));
         DocumentCollection ontonotes = DocumentCollection.create("conll::/home/ik/Downloads/streusle_test.txt;fields=WORD,SUPER_SENSE;docPerSentence=True")
                                                          .annotate(Types.PART_OF_SPEECH, Types.PHRASE_CHUNK).cache();
         CoNLLEvaluation evaluation = new CoNLLEvaluation("label");
         DataSet ds = ner.transform(ontonotes);
         evaluation.evaluate(ner.delegate(), ds);
         evaluation.report();
         Document doc = Document.create("A Vatican spokesman confirmed later Wednesday that Benedict’s health had worsened “in the last few hours” and that Francis visited Benedict at the Mater Ecclesiae monastery in Vatican City.");
         doc.annotate(Types.PART_OF_SPEECH);
         doc.annotate(Types.SUPER_SENSE);
         for (Annotation annotation : doc.annotations(Types.SUPER_SENSE)) {
            System.out.println(annotation.toSGML(true));
         }

      }
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(TOKENS, valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence(CHARS, booleanFeaturizer(h -> h.charNGrams(1)
                                                                                  .stream()
                                                                                  .map(HString::toString)
                                                                                  .collect(toList())))
                                    .tokenSequence(POS, valueFeaturizer(h -> h.pos().getUniversalTag().name()))
                                    .source(LABEL, IOB.encoder(Types.SUPER_SENSE))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "1.2";
   }
}
