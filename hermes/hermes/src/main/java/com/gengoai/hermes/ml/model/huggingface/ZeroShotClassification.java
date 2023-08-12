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
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZeroShotClassification implements AutoCloseable {

    public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";

    private final PythonInterpreter interpreter;

    public ZeroShotClassification(@NonNull String modelName, boolean multiLabel, int device) {
        this(modelName, modelName, multiLabel, device);
    }

    public ZeroShotClassification(@NonNull String modelName, @NonNull String tokenizerName, boolean multiLabel, int device) {
        this.interpreter = new PythonInterpreter(String.format("from transformers import pipeline\n" +
                                                                       "nlp = pipeline('zero-shot-classification', model='%s', tokenizer='%s', device=%d)\n" +
                                                                       "def pipe(sentence,labels):\n" +
                                                                       "    return nlp(list(sentence),list(labels), multi_label=%s)", modelName,
                                                               tokenizerName,
                                                               device,
                                                               multiLabel ? "True" : "False"));


    }


    public List<Counter<String>> predict(@NonNull List<String> sequences, @NonNull List<String> candidateLabels) {
        List<Map<String, ?>> rvals = Cast.as(interpreter.invoke("pipe",
                                                                sequences,
                                                                candidateLabels));
        return rvals.stream().map(rval -> {
            List<String> labels = Cast.as(rval.get("labels"));
            List<Double> scores = Cast.as(rval.get("scores"));
            Counter<String> counter = Counters.newCounter();
            for (int i = 0; i < labels.size(); i++) {
                counter.set(labels.get(i), scores.get(i));
            }
            return counter;
        }).collect(Collectors.toList());
    }

    public Counter<String> predict(@NonNull String sequence, @NonNull List<String> candidateLabels) {
        return predict(List.of(sequence), candidateLabels).get(0);
    }

    public static void main(String[] args) {
        ZeroShotClassification zsc = new ZeroShotClassification(BART_LARGE_MNLI, true, 0);
        System.out.println(zsc.predict(List.of("I am happy", "I am sad"),
                                       List.of("happy", "sad")));
    }

    @Override
    public void close() throws Exception {
        interpreter.close();
    }

}//END OF ZeroShotClassification
