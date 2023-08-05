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

import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenClassification {
    public static final String BERT_BASE_NER = "dslim/bert-base-NER";

    private final PythonInterpreter interpreter;

    public TokenClassification(@NonNull String modelName,
                               int device) {
        this(modelName, modelName, device);
    }

    public TokenClassification(@NonNull String modelName,
                               @NonNull String tokenizerName,
                               int device) {
//        this.interpreter = new PythonInterpreter("""
//                from transformers import pipeline
//
//                nlp = pipeline('ner', model="%s", tokenizer="%s", device=%d, aggregation_strategy="simple")
//
//                def pipe(context):
//                   return nlp(list(context))
//                      """.formatted(modelName, tokenizerName, device));
        this.interpreter = new PythonInterpreter(String.format("from transformers import pipeline\n" +
                                                                       "nlp = pipeline('ner', model='%s', tokenizer='%s', device=%d, aggregation_strategy='simple')\n" +
                                                                       "def pipe(context):\n" +
                                                                       "   return nlp(list(context))\n", modelName, tokenizerName, device));
    }


    @Value
    public static class Output {
        String label;
        double confidence;
        int start;
        int end;
    }

    public List<List<Output>> predict(List<String> contexts) {
        List<List<Map<String, ?>>> rvals = Cast.as(interpreter.invoke("pipe", contexts));
        return rvals.stream().map(l -> l.stream().map(m -> new Output(
                                          m.get("entity_group").toString(),
                                          Cast.as(m.get("score"), Number.class).doubleValue(),
                                          Cast.as(m.get("start"), Number.class).intValue(),
                                          Cast.as(m.get("end"), Number.class).intValue()
                                  )).collect(Collectors.toList())
                                 ).collect(Collectors.toList());
    }

    public List<Output> predict(String context) {
        return predict(List.of(context)).get(0);
    }

    public static void main(String[] args) {
        TokenClassification tc = new TokenClassification(BERT_BASE_NER, 0);
        System.out.println(tc.predict("John met Mary in Japan."));
    }

}//END OF TokenClassification
