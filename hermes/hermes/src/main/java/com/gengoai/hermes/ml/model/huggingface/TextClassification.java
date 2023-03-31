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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextClassification extends HuggingFacePipeline<String, Counter<String>> {

    public TextClassification(@NonNull String modelName,
                              @NonNull String tokenizerName,
                              int device) {
        super("""
                from transformers import pipeline
                nlp = pipeline('text-classification', model="%s", tokenizer="%s", device=%d, top_k=None)
                                
                def pipe(text):
                    return nlp(list(text))""".formatted(modelName,
                tokenizerName,
                device));
    }

    @Override
    public Counter<String> predict(@NonNull String s) {
        List<List<Map<String, ?>>> rvals = Cast.as(interpreter.invoke("pipe", s));
        Counter<String> counter = Counters.newCounter();
        for (Map<String, ?> stringMap : rvals.get(0)) {
            counter.set(stringMap.get("label").toString(),
                    Cast.as(stringMap.get("score"), Double.class));
        }
        return counter;
    }

    @Override
    public List<Counter<String>> predict(@NonNull List<String> strings) {
        List<List<Map<String, ?>>> rvals = Cast.as(interpreter.invoke("pipe", strings));
        List<Counter<String>> counterList = new ArrayList<>();
        for (List<Map<String, ?>> rList : rvals) {
            Counter<String> counter = Counters.newCounter();
            for (Map<String, ?> stringMap : rList) {
                counter.set(stringMap.get("label").toString(),
                        Cast.as(stringMap.get("score"), Double.class));
            }
            counterList.add(counter);
        }
        return counterList;
    }

    public static void main(String[] args) {
        TextClassification textClassification = new TextClassification(
                "roberta-large-mnli",
                "roberta-large-mnli",
                0
        );
        System.out.println(textClassification.predict("I hated the food."));
    }

}
