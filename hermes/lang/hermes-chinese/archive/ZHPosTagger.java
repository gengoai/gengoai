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

package com.gengoai.hermes.zh;

import com.gengoai.apollo.data.DataSetType;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.transform.MinCountFilter;
import com.gengoai.apollo.feature.FeatureExtractor;
import com.gengoai.apollo.feature.Featurizer;
import com.gengoai.apollo.model.Params;
import com.gengoai.apollo.model.PipelineModel;
import com.gengoai.apollo.model.sequence.GreedyAvgPerceptron;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.en.ENPOSValidator;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.feature.AffixFeaturizer;
import com.gengoai.hermes.ml.feature.Features;
import com.gengoai.hermes.ml.model.POSTagger;

import static com.gengoai.hermes.ml.feature.Features.LowerCaseWord;

public class ZHPosTagger extends POSTagger {
    private static final long serialVersionUID = 1L;

    private static FeatureExtractor<HString> createFeatureExtractor() {
        return Featurizer.chain(new AffixFeaturizer(3, 3),
                                 LowerCaseWord,
                                 Features.charNGrams(2))
                         .withContext("LowerWord[-1]",
                                 "~LowerWord[-2]",
                                 "LowerWord[+1]",
                                 "~LowerWord[+2]");
    }

    public ZHPosTagger() {
        super(HStringDataSetGenerator.builder(Types.SENTENCE)
                                     .dataSetType(DataSetType.InMemory)
                                     .tokenSequence(Datum.DEFAULT_INPUT, createFeatureExtractor())
                                     .tokenSequence(Datum.DEFAULT_OUTPUT, h -> Variable.binary(h.pos().name()))
                                     .build(),
                PipelineModel.builder()
                             .defaultInput(new MinCountFilter(5))
                             .build(new GreedyAvgPerceptron(parameters -> {
                                 parameters.set(Params.Optimizable.maxIterations, 50);
                                 parameters.set(Params.verbose, true);
                                 parameters.set(Params.Optimizable.historySize, 3);
                                 parameters.set(Params.Optimizable.tolerance, 1e-4);
//                                 parameters.validator.set(new ENPOSValidator());
                             })));
    }

    @Override
    public String getVersion() {
        return "2.1";
    }
}
