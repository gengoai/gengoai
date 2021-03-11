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

import com.gengoai.Language;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.feature.Featurizer;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.collection.Maps;
import com.gengoai.config.Config;
import com.gengoai.hermes.*;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.io.Resources;

import java.util.Map;
import java.util.stream.Collectors;

import static com.gengoai.apollo.encoder.FixedEncoder.fixedEncoder;
import static com.gengoai.apollo.encoder.IndexEncoder.indexEncoder;
import static com.gengoai.apollo.encoder.IndexEncoder.iobLabelEncoder;
import static com.gengoai.apollo.model.TFVarSpec.varSpec;
import static com.gengoai.hermes.ResourceType.WORD_LIST;
import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class NeuralNERModel extends TensorFlowSequenceLabeler implements HStringMLModel {
   private static final String LABEL = "label";
   private static final String TOKENS = "words";
   private static final String CHARS = "chars";
   private static final String SHAPE = "shape";
   private static final int MAX_WORD_LENGTH = 10;


   public NeuralNERModel() {
      super(Map.of(TOKENS, varSpec(TOKENS,
                                   fixedEncoder(WORD_LIST.locate("glove", Language.ENGLISH)
                                                         .orElseThrow(), "--UNKNOWN--"),
                                   -1),
                   SHAPE, varSpec(SHAPE, indexEncoder("-PAD-"), -1),
                   CHARS, varSpec(CHARS, indexEncoder("-PAD-"), -1, MAX_WORD_LENGTH)),
            Maps.linkedHashMapOf($(LABEL, varSpec("label/truediv", iobLabelEncoder(), -1))),
            IOBValidator.INSTANCE,
            IOB.decoder(Types.ML_ENTITY));
   }

   public static void main(String[] args) throws Exception {
      Config.initialize("CNN", args, "com.gengoai.hermes");
      if (Math.random() < 0) {
         NeuralNERModel ner = new NeuralNERModel();
         DocumentCollection ontonotes = Corpus.open("/data/corpora/hermes_data/ontonotes_ner")
                                              .query("$SPLIT='TRAIN'");
         ner.estimate(ontonotes);
         ModelIO.save(ner, Resources.from("/data/hermes/en/models/ner-tmp"));
      } else {
         NeuralNERModel ner = ModelIO.load(Resources.from("/Volumes/ikdata-1/hermes/en/models/ner-reslstm"));
      DocumentCollection ontonotes = Corpus.open("/Users/ik/hermes_data/ontonotes_ner")
                                           .query("$SPLIT='TEST'");
      CoNLLEvaluation evaluation = new CoNLLEvaluation("label");
      evaluation.evaluate(ner.delegate(), ner.transform(ontonotes));
      evaluation.report();

         Document document = Document.create("John Bowman sailed to the Virgin Islands last Saturday");
         document.annotate(Types.ENTITY);
         for (Annotation annotation : document.annotations(Types.ENTITY)) {
            System.out.println(annotation.toSGML(true));
         }

         ElmoTokenEmbedding elmo = ResourceType.MODEL.load("elmo", Language.ENGLISH);
         elmo.apply(document);
         for (Annotation token : document.tokens()) {
            System.out.println(token + ": " + token.embedding());
         }

         UniversalSentenceEncoder use = ResourceType.MODEL.load("sentence_encoder", Language.ENGLISH);
         use.apply(document);
         for (Annotation sentence : document.sentences()) {
            System.out.println(sentence);
            System.out.println(sentence.embedding());
         }

      }
   }


   @Override
   protected int calculate_max_sequence_length(DataSet batch) {
      return (int) batch.stream().mapToDouble(d -> d.get(TOKENS).asNDArray().rows()).max().orElse(0d);
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(TOKENS, Featurizer.valueFeaturizer(HString::toLowerCase))
                                    .tokenSequence(CHARS, Featurizer.booleanFeaturizer(h -> h.charNGrams(1)
                                                                                             .stream()
                                                                                             .map(HString::toLowerCase)
                                                                                             .collect(Collectors
                                                                                                            .toList())))
                                    .tokenSequence(SHAPE, Features.WordShape)
                                    .source(LABEL, IOB.encoder(Types.ENTITY))
                                    .build();
   }

   @Override
   public String getVersion() {
      return "1.1";
   }

}//END OF CNNNer
