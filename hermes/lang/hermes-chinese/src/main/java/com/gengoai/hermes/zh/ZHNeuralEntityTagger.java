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

import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.IOBValidator;
import com.gengoai.hermes.ml.model.TFSequenceLabeler;

import java.util.List;

import static com.gengoai.apollo.feature.Featurizer.booleanFeaturizer;
import static java.util.stream.Collectors.toList;

public class ZHNeuralEntityTagger extends TFSequenceLabeler {
    private static final String CHARS = "chars";
    private static final int MAX_WORD_LENGTH = 25;
    private static final String LABEL = "label";

    public ZHNeuralEntityTagger() {
        super(List.of(
                        TFInputVar.sequence(CHARS,
                                "serving_default_chars",
                                -1, MAX_WORD_LENGTH)
                     ),
                List.of(
                        TFOutputVar.sequence(LABEL,
                                "StatefulPartitionedCall:0",
                                "O", IOBValidator.INSTANCE)
                       ),
                IOB.decoder(Types.ML_ENTITY));
    }

    @Override
    public HStringDataSetGenerator getDataGenerator() {
        return HStringDataSetGenerator.builder(Types.SENTENCE)
                                      .tokenSequence(CHARS, booleanFeaturizer(h -> h.charNGrams(1)
                                                                                    .stream()
                                                                                    .map(HString::toString)
                                                                                    .collect(toList())))
                                      .source(LABEL, IOB.encoder(Types.ENTITY))
                                      .build();
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

}
