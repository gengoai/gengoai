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
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class QuestionAnswering {
    public static final String ROBERTA_BASE_SQUAD = "deepset/roberta-base-squad2";


    private final String uniqueFuncName;

    public QuestionAnswering(@NonNull String modelName) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1));
    }

    public QuestionAnswering(@NonNull String modelName,
                             int device) {
        this(modelName, modelName, device);
    }

    public QuestionAnswering(@NonNull String modelName,
                             @NonNull String tokenizerName,
                             int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import pipeline\n" +
                                                     uniqueFuncName + "_nlp = pipeline('question-answering', model=\"%s\", tokenizer=\"%s\", device=%d )\n" +
                                                     "def " + uniqueFuncName + "(context, question):\n" +
                                                     "   return " + uniqueFuncName + "_nlp({'context':list(context),'question':list(question)})\n", modelName, tokenizerName, device));
    }

    public QAOutput predict(@NonNull String context, @NonNull String question) {
        return predict(List.of(new QAInput(context, question))).get(0);
    }


    public QAOutput predict(@NonNull QAInput input) {
        return predict(List.of(input)).get(0);
    }

    public List<QAOutput> predict(@NonNull List<QAInput> inputs) {
        List<HashMap<String, ?>> rvals = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName,
                                                                                        inputs.stream().map(QAInput::getContext).collect(Collectors.toList()),
                                                                                        inputs.stream().map(QAInput::getQuestion).collect(Collectors.toList())
                                                                                       ));
        return rvals.stream().map(m -> new QAOutput(
                m.get("answer").toString(),
                Cast.as(m.get("score"), Number.class).doubleValue(),
                Cast.as(m.get("start"), Number.class).intValue(),
                Cast.as(m.get("end"), Number.class).intValue()
        )).collect(Collectors.toList());
    }

    @Value
    public static class QAOutput {
        String answer;
        double score;
        int startChar;
        int endChar;
    }

    @Value
    public static class QAInput {
        String context;
        String question;
    }

    public static void main(String[] args) {
        QuestionAnswering generator = new QuestionAnswering(QuestionAnswering.ROBERTA_BASE_SQUAD, 0);
        System.out.println(generator.predict("god is a type of deity.", "what is god?"));
    }

}//END OF QuestionAnswering
