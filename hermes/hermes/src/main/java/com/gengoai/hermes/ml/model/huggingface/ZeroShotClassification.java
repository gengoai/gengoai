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

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public class ZeroShotClassification extends HuggingFacePipeline<ZeroShotClassification.ZeroShotInput, Counter<String>> {

    public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";


    public ZeroShotClassification() {
        this(BART_LARGE_MNLI, BART_LARGE_MNLI, false, -1);
    }

    public ZeroShotClassification(@NonNull String modelName,
                                  boolean multiLabel,
                                  int device) {
        this(modelName, modelName, multiLabel, device);
    }

    public ZeroShotClassification(@NonNull String modelName,
                                  @NonNull String tokenizerName,
                                  boolean multiLabel,
                                  int device) {
        super("""
                from transformers import pipeline
                nlp = pipeline('zero-shot-classification', model="%s", tokenizer="%s", device=%d)
                                
                def pipe(sentence,labels):
                    return nlp(list(sentence),list(labels), multi_label=%s)""".formatted(modelName,
                tokenizerName,
                device,
                multiLabel ? "True" : "False"));
    }

    public static ZeroShotInput createInput(String sequence, List<String> labels) {
        return new ZeroShotInput(sequence, labels);
    }

    @Override
    public Counter<String> predict(@NonNull ZeroShotInput zeroShotInput) {
        Map<String, ?> rvals = Cast.as(Cast.as(interpreter.invoke("pipe",
                List.of(zeroShotInput.sequence),
                zeroShotInput.labels), List.class).get(0));
        List<String> labels = Cast.as(rvals.get("labels"));
        List<Double> scores = Cast.as(rvals.get("scores"));
        Counter<String> counter = Counters.newCounter();
        for (int i = 0; i < labels.size(); i++) {
            counter.set(labels.get(i), scores.get(i));
        }
        return counter;
    }

    public record ZeroShotInput(String sequence, List<String> labels) {
    }

}//END OF ZeroShotClassification
