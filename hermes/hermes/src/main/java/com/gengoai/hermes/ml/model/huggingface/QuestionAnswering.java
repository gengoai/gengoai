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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

public class QuestionAnswering extends HuggingFacePipeline<QuestionAnswering.QAInput, QuestionAnswering.QAOutput> {
    public static final String ROBERTA_BASE_SQUAD = "deepset/roberta-base-squad2";

    @Override
    public List<QAOutput> predict(@NonNull List<QAInput> qaInputs) {
        return qaInputs.stream().map(this::predict).toList();
    }

    @Override
    public QAOutput predict(@NonNull QAInput qaInput) {
        HashMap<String, ?> m = Cast.as(interpreter.invoke("pipe", qaInput.context, qaInput.question));
        return new QAOutput(
                m.get("answer").toString(),
                Cast.as(m.get("score"), Number.class).doubleValue(),
                Cast.as(m.get("start"), Number.class).intValue(),
                Cast.as(m.get("end"), Number.class).intValue()
        );
    }

    public record QAInput(String context, String question) {
    }

    public record QAOutput(String answer, double score, int startChart, int endChar) {
    }


    public QuestionAnswering() {
        this(ROBERTA_BASE_SQUAD, -1);
    }

    public QuestionAnswering(@NonNull String modelName, int device) {
        super("""
                from transformers import AutoModelForQuestionAnswering, AutoTokenizer, pipeline

                nlp = pipeline('question-answering', model="%s", tokenizer="%s", device=%d) 
                                                                                    
                def pipe(context, question):
                   return nlp(context=context,question=question)
                      """.formatted(modelName, modelName, device));
    }

    public QAResult predict(String question, String context) {
        HashMap<String, ?> m = Cast.as(interpreter.invoke("pipe", question, context));
        return new QAResult(
                m.get("answer").toString(),
                Cast.as(m.get("score"), Number.class).doubleValue(),
                Cast.as(m.get("start"), Number.class).intValue(),
                Cast.as(m.get("end"), Number.class).intValue()
        );
    }

    @Value
    public static class QAResult implements Serializable {
        private static final long serialVersionUID = 1L;
        String answer;
        double score;
        int startChar;
        int endChar;
    }

    public static void main(String[] args) {
        QuestionAnswering qa = new QuestionAnswering(ROBERTA_BASE_SQUAD, 0);
        System.out.println(qa.predict(new QAInput(
                "Florida is a state in the United States of America. Its capital is Tallahassee.",
                "What is the capital of Florida?")));
    }

}
