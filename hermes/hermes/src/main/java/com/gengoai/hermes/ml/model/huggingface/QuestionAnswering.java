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

import java.util.HashMap;
import java.util.List;

public class QuestionAnswering {
    public static final String ROBERTA_BASE_SQUAD = "deepset/roberta-base-squad2";

    private final PythonInterpreter interpreter;

    public QuestionAnswering(@NonNull String modelName,
                             int device) {
        this(modelName, modelName, device);
    }

    public QuestionAnswering(@NonNull String modelName,
                             @NonNull String tokenizerName,
                             int device) {
        this.interpreter = new PythonInterpreter("""
                from transformers import AutoModelForQuestionAnswering, AutoTokenizer, pipeline

                nlp = pipeline('question-answering', model="%s", tokenizer="%s", device=%d) 
                                                                                    
                def pipe(context, question):
                   return nlp({'context':list(context),'question':list(question)})
                      """.formatted(modelName, tokenizerName, device));
    }

    public QAOutput predict(@NonNull String context, @NonNull String question) {
        return predict(List.of(new QAInput(context, question))).get(0);
    }


    public QAOutput predict(@NonNull QAInput input) {
        return predict(List.of(input)).get(0);
    }

    public List<QAOutput> predict(@NonNull List<QAInput> inputs) {
        List<HashMap<String, ?>> rvals = Cast.as(interpreter.invoke("pipe",
                inputs.stream().map(QAInput::context).toList(),
                inputs.stream().map(QAInput::question).toList()
        ));
        return rvals.stream().map(m -> new QAOutput(
                m.get("answer").toString(),
                Cast.as(m.get("score"), Number.class).doubleValue(),
                Cast.as(m.get("start"), Number.class).intValue(),
                Cast.as(m.get("end"), Number.class).intValue()
        )).toList();
    }

    public record QAOutput(String answer, double score, int startChart, int endChar) {
    }

    public record QAInput(@NonNull String context, @NonNull String question) {

    }

}//END OF QuestionAnswering
