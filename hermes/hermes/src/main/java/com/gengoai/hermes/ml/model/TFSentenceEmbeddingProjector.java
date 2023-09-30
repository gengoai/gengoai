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
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.collection.Iterators;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;

public abstract class TFSentenceEmbeddingProjector extends TFEmbeddingProjector {
    public static final String TOKENS = "words";
    private final String outputName;

    public TFSentenceEmbeddingProjector(@NonNull List<TFInputVar> inputVars, String outputName, String outputServingName) {
        super(inputVars, outputName, outputServingName);
        this.outputName = outputName;
    }

    @Override
    public HStringDataSetGenerator getDataGenerator() {
        return HStringDataSetGenerator.builder(Types.SENTENCE)
                                      .tokenSequence(TOKENS, valueFeaturizer(HString::toString))
                                      .build();
    }


    @Override
    public HString apply(HString hString) {
        DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
        Iterators.zip(hString.sentences().iterator(), processBatch(dataSet).iterator())
                 .forEachRemaining(e -> {
                     Annotation sentence = e.getKey();
                     NumericNDArray embeddings = e.getValue().get(outputName).asNumericNDArray();
                     sentence.put(Types.EMBEDDING, embeddings.toFloatArray());
                 });
        return hString;
    }
}
