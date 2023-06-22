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
import com.gengoai.apollo.model.Consts;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.config.Config;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.en.ENResources;
import com.gengoai.hermes.ml.CoNLLEvaluation;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.IOBValidator;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.io.Resources;

import java.util.List;

import static com.gengoai.apollo.encoder.FixedEncoder.fixedEncoder;
import static com.gengoai.apollo.feature.Featurizer.booleanFeaturizer;
import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;
import static java.util.stream.Collectors.toList;

public class NeuralNERModel extends TFSequenceLabeler {
    private static final long serialVersionUID = 1L;
    private static final String LABEL = "label";
    private static final String TOKENS = "words";
    private static final String CHARS = "chars";
    private static final String SHAPE = "shape";
    private static final int MAX_WORD_LENGTH = 25;

    public NeuralNERModel() {
        super(
                List.of(
                        TFInputVar.sequence(TOKENS,
//                                "serving_default_words",
                                fixedEncoder(ENResources.gloveSmallLexicon(), Consts.UNKNOWN_WORD)),
                        TFInputVar.sequence(SHAPE,
//                                "serving_default_shape",
                                -1),
                        TFInputVar.sequence(CHARS,
//                                "serving_default_chars",
                                -1, MAX_WORD_LENGTH)
                ),
                List.of(
                        TFOutputVar.sequence(LABEL,
//"StatefulPartitionedCall:0",
                                "label/truediv",
                                "O", IOBValidator.INSTANCE)
                ),
                IOB.decoder(Types.ML_ENTITY)
        );
    }

    public static void main(String[] args) throws Exception {
        Config.initialize("CNN", args, "com.gengoai.hermes");
        if (Math.random() < 0) {
            Config.setProperty("tfmodel.data", "/work/prj/gengoai/python/tensorflow/data/entity.db");
            NeuralNERModel ner = new NeuralNERModel();
            DocumentCollection ontonotes = Corpus.open("/shared/ikdata/corpora/hermes_data/ontonotes_ner")
                    .query("$SPLIT='TRAIN'");
            ner.estimate(ontonotes);
            ModelIO.save(ner, Resources.from("/home/ik/hermes/en/models/ner"));
        } else {
            NeuralNERModel ner = ModelIO.load(Resources.from("/home/ik/hermes/en/models/ner/"));
            DocumentCollection ontonotes = Corpus.open("/shared/ikdata/corpora/hermes_data/ontonotes_ner")
                    .query("$SPLIT='TEST'");
            CoNLLEvaluation evaluation = new CoNLLEvaluation("label");
            DataSet ds = ner.transform(ontonotes);
            evaluation.evaluate(ner.delegate(), ds);
            evaluation.report();
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
                .tokenSequence(SHAPE, Features.WordShape)
                .source(LABEL, IOB.encoder(Types.ENTITY))
                .build();
    }

    @Override
    public String getVersion() {
        return "1.2";
    }
}
