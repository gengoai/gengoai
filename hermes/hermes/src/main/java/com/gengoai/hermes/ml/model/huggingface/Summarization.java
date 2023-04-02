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
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public class Summarization implements Summarizer {
    public static final String FLAN_T5_BASE_SAMSUM = "philschmid/flan-t5-base-samsum";

    private final PythonInterpreter interpreter;

    public Summarization(@NonNull String modelName,
                         int device,
                         int maxLength) {
        this(modelName, modelName, device, maxLength);
    }

    public Summarization(@NonNull String modelName,
                         @NonNull String tokenizerName,
                         int device,
                         int maxLength) {
        this.interpreter = new PythonInterpreter("""
                from transformers import pipeline

                nlp = pipeline('summarization', model="%s", tokenizer="%s", device=%d)
                                                                                    
                def pipe(context):
                   return nlp(list(context), max_length=%d)
                      """.formatted(modelName, tokenizerName, device, maxLength));
    }


    @Override
    public Extraction extract(@NonNull HString hString) {
        return Extraction.fromStringList(List.of(predict(hString.toString())));
    }


    @Override
    public void fit(@NonNull DocumentCollection corpus) {

    }

    public String predict(@NonNull String text) {
        return predict(List.of(text)).get(0);
    }

    public List<String> predict(@NonNull List<String> texts) {
        List<Map<String, ?>> rvals = Cast.as(interpreter.invoke("pipe", texts));
        return rvals.stream().map(rList -> rList.get("summary_text").toString()).toList();
    }

}//END OF Summarization
