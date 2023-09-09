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
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TextGeneration {
    public static final String GPT2_MEDIUM = "gpt2-medium";

    private final String uniqueFuncName;

    public TextGeneration(@NonNull String modelName) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1));
    }

    public TextGeneration(@NonNull String modelName,
                          int device) {
        this(modelName, modelName, device);
    }

    public TextGeneration(@NonNull String modelName,
                          @NonNull String tokenizerName,
                          int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import pipeline\n" +
                                                     uniqueFuncName + "_nlp = pipeline('text-generation', model=\"%s\", tokenizer=\"%s\", device=%d)\n" +
                                                     "def " + uniqueFuncName + "(context,max_length):\n" +
                                                     "   return " + uniqueFuncName + "_nlp(list(context), max_length=max_length)\n", modelName, tokenizerName, device));
    }


    public List<String> predict(List<String> contexts, int maxLength) {
        List<List<Map<String, ?>>> rvals = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, contexts, maxLength));
        return rvals.stream().map(l -> l.get(0).get("generated_text").toString()).collect(Collectors.toList());
    }

    public String predict(String context, int maxLength) {
        return predict(List.of(context), maxLength).get(0);
    }

    public static void main(String[] args) {
        TextGeneration tg = new TextGeneration(GPT2_MEDIUM, 0);
        System.out.println(tg.predict("I went to the store and ", 50));
    }

}//END OF TextGeneration
