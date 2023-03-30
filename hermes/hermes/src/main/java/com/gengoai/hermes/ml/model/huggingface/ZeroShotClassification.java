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

package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.apollo.data.observation.Classification;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import com.gengoai.stream.Streams;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

public class ZeroShotClassification extends HuggingFacePipeline<ZeroShotClassification.ZeroShotInput, ZeroShotClassification.ZeroShotOutput> {

    public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";


    public ZeroShotClassification() {
        this(BART_LARGE_MNLI, false, -1);
    }

    public ZeroShotClassification(@NonNull String modelName, boolean multiLabel, int device) {
        super("""
                from transformers import pipeline
                nlp = pipeline('zero-shot-classification', model="%s", tokenizer="%s", device=%d)
                                
                def pipe(sentence,labels):
                    return nlp(sentence,labels, multi_label=%s)""".formatted(modelName,
                modelName,
                device,
                multiLabel ? "True" : "False"));
    }

    public static ZeroShotInput createInput(String sequence, List<String> labels) {
        return new ZeroShotInput(sequence, labels);
    }

    @Override
    public ZeroShotOutput predict(@NonNull ZeroShotInput zeroShotInput) {
        Map<String, ?> rvals = Cast.as(interpreter.invoke("pipe", zeroShotInput.sequence, zeroShotInput.labels));
        return new ZeroShotOutput(Cast.as(rvals.get("sequence")),
                Cast.as(rvals.get("labels")),
                Cast.as(rvals.get("scores")));
    }

    @Override
    public List<ZeroShotOutput> predict(@NonNull List<ZeroShotInput> zeroShotInputs) {
        return zeroShotInputs.stream().map(this::predict).toList();
    }

    public record ZeroShotInput(String sequence, List<String> labels) {
    }

    public record ZeroShotOutput(String sequence, List<String> labels, List<Double> scores) {

    }


}
