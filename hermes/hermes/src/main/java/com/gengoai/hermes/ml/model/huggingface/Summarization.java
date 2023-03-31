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
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.summarization.Summarizer;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Summarization extends HuggingFacePipeline<String, String> implements Summarizer {
    public static final String FLAN_T5_BASE_SAMSUM = "philschmid/flan-t5-base-samsum";

    public Summarization() {
        this(FLAN_T5_BASE_SAMSUM, FLAN_T5_BASE_SAMSUM, -1, 100);
    }

    public Summarization(int maxLength) {
        this(FLAN_T5_BASE_SAMSUM, FLAN_T5_BASE_SAMSUM, -1, maxLength);
    }

    public Summarization(@NonNull String modelName,
                         @NonNull String tokenizerName,
                         int device,
                         int maxLength) {
        super("""
                from transformers import pipeline

                nlp = pipeline('summarization', model="%s", tokenizer="%s", device=%d)
                                                                                    
                def pipe(context):
                   return nlp(list(context), max_length=%d)
                      """.formatted(modelName, tokenizerName, device, maxLength));
    }


    @Override
    public Extraction extract(@NonNull HString hString) {
        List<?> m = Cast.as(interpreter.invoke("pipe", hString.toString()));
        String summary_text = ((Map<String, String>) m.get(0)).get("summary_text");
        return Extraction.fromStringList(List.of(summary_text));
    }


    @Override
    public void fit(@NonNull DocumentCollection corpus) {

    }

    @Override
    public String predict(@NonNull String s) {
        List<?> m = Cast.as(interpreter.invoke("pipe", s));
        return ((Map<String, String>) m.get(0)).get("summary_text");
    }

    @Override
    public List<String> predict(@NonNull List<String> strings) {
        List<Map<String, ?>> rvals = Cast.as(interpreter.invoke("pipe", strings));
        List<String> summaries = new ArrayList<>();
        for (Map<String, ?> rList : rvals) {
            summaries.add(rList.get("summary_text").toString());
        }
        return summaries;
    }

}//END OF Summarization
