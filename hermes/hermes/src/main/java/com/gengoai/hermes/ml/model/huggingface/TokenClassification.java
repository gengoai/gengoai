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

import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.*;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.Resources;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TokenClassification {
    public static final String BERT_BASE_NER = "dslim/bert-base-NER";

    private final String uniqueFuncName;


    public TokenClassification(@NonNull String modelName) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1));
    }

    public TokenClassification(@NonNull String modelName,
                               int device) {
        this(modelName, modelName, device);
    }

    public TokenClassification(@NonNull String modelName,
                               @NonNull String tokenizerName,
                               int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import pipeline\n" +
                                                     uniqueFuncName + "_nlp = pipeline('ner', model='%s', tokenizer='%s', device=%d, aggregation_strategy='max')\n" +
                                                     "def " + uniqueFuncName + "(context):\n" +
                                                     "   return " + uniqueFuncName + "_nlp(list(context))\n", modelName, tokenizerName, device));
    }


    @Value
    public static class Output {
        String label;
        double confidence;
        int start;
        int end;
    }

    public List<List<Output>> predict(List<String> contexts) {
        List<List<Map<String, ?>>> rvals = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, contexts));
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
        Config.initialize("Sandbox", args);
        TokenClassification tc = new TokenClassification("/work/prj/huggingface/token-classification/bert-finetuned-ner", 0);
        Corpus docs = Corpus.open("/work/shortDocs.corpus");
        docs.clearAnnotations();
        Resources.from("/work/magic.corpus").delete(true);
        Corpus c = Corpus.open("/work/magic.corpus");
        List<Document> buffer = new ArrayList<>();
        for (Document d : docs) {
            d.annotate(Types.SENTENCE);
            for (Annotation sentence : d.sentences()) {
                System.out.println(sentence);
                List<Output> outputs = tc.predict(sentence.toString());
                for (Output output : outputs) {
                    HString eHStr = sentence.substring(output.start, output.end);
                    d.createAnnotation(Types.ENTITY,
                                       eHStr.start(),
                                       eHStr.end(),
                                       Map.of(Types.ENTITY_TYPE, EntityType.valueOf(output.label),
                                              Types.CONFIDENCE, output.confidence));
                }
            }
            for (Annotation entity : d.annotations(Types.ENTITY)) {
                System.out.println(entity + " / " + entity.attribute(Types.ENTITY_TYPE) + " / " + entity.attribute(Types.CONFIDENCE));
            }
            buffer.add(d);

            if (buffer.size() > 100) {
                c.addAll(buffer);
                buffer.clear();
            }

        }

        c.addAll(buffer);

    }

}//END OF TokenClassification
