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

package com.gengoai.hermes.zh.training;

import com.gengoai.Language;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.apollo.feature.ObservationExtractor;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.sequence.Crf;
import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.zh.CharacterBILU;
import com.gengoai.hermes.zh.ZHSegmentationExtractor;
import com.gengoai.io.Resources;

public class ZHSegmentationTrainer {


    public static void main(String[] args) throws Exception {
        Config.initialize("Sandbox", args, "com.gengoai.hermes");

        var onto = "conll::/shared/OneDrive/corpora/Ontonotes-5.0/combined.conll/;defaultLanguage=CHINESE;docPerSentence=true;fields=[WORD,POS]";
        var trainDocs = DocumentCollection.create(onto);
        trainDocs = trainDocs.filter(d -> d.tokenStream().noneMatch(t -> t.pos() == PartOfSpeech.ANY));
        ZHSegmentationExtractor segmentationExtractor = new ZHSegmentationExtractor(ResourceType.WORD_LIST.load("dictionary", Language.CHINESE),
                                                                                    3);

        ObservationExtractor<HString> binaryLabeler = s -> {
            VariableSequence label = new VariableSequence();
            int end = 0;
            for (Annotation token : s.tokens()) {
                for (int i = 0; i < token.length() - 1; i++) {
                    label.add(Variable.binary("false"));
                }
                label.add(Variable.binary("true"));
                end = token.end();
                while (end < s.length() && Character
                        .isWhitespace(s.charAt(end))) {
                    label.add(Variable.binary("true"));
                    end++;
                }
            }
            return label;
        };

        var generator = HStringDataSetGenerator.builder(Types.SENTENCE)
                                               .defaultInput(segmentationExtractor)
                                               .defaultOutput(CharacterBILU.encoder).build();
        DataSet train = trainDocs.asDataSet(generator);
        Crf crf = new Crf();
        crf.getFitParameters().maxIterations.set(100);
        crf.getFitParameters().minFeatureFreq.set(10);

        crf.fitAndTransform(train);
        ModelIO.save(crf, Resources.from("/home/ik/segmentation"));
    }

}
