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

public class FillMaskGenerator extends HuggingFacePipeline<String, Counter<String>> {
    public static final String BERT_BASE_UNCASED = "bert-base-uncased";
    public static final String BERT_MASK_TOKEN = "[MASK]";
    public static final String XLM_ROBERTA_LARGE = "xlm-roberta-large";
    public static final String ROBERTA_MASK_TOKEN = "<mask>";


    public FillMaskGenerator() {
        this(XLM_ROBERTA_LARGE, XLM_ROBERTA_LARGE, -1);
    }

    public FillMaskGenerator(@NonNull String modelName,
                             @NonNull String tokenizerName,
                             int device) {
        super("""
                from transformers import pipeline

                nlp = pipeline('fill-mask', model="%s", tokenizer="%s", device=%d)
                                                                                    
                def pipe(context):
                   return nlp(context)
                      """.formatted(modelName, tokenizerName, device));
    }

    public Counter<String> predict(String context) {
        List<?> m = Cast.as(interpreter.invoke("pipe", context));
        Counter<String> rval = Counters.newCounter();
        for (Object o : m) {
            Map<String, ?> map = Cast.as(o);
            String tokenStr = map.get("token_str").toString();
            double score = ((Number) map.get("score")).doubleValue();
            rval.set(tokenStr, score);
        }
        return rval;
    }

}//END OF FillMaskGenerator
