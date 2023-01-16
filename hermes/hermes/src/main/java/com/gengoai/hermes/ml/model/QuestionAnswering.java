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

package com.gengoai.hermes.ml.model;

import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import com.gengoai.stream.Streams;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import lombok.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

public class QuestionAnswering {
   public static final String ROBERTA_BASE_SQUAD = "deepset/roberta-base-squad2";

   private final PythonInterpreter interpreter;


   public QuestionAnswering() {
      this(ROBERTA_BASE_SQUAD);
   }

   public QuestionAnswering(@NonNull String modelName) {
      this.interpreter = new PythonInterpreter("""
                                                     from transformers import AutoModelForQuestionAnswering, AutoTokenizer, pipeline

                                                     nlp = pipeline('question-answering', model="%s", tokenizer="%s")   
                                                                                                                         
                                                     def pipe(question,context):
                                                        return nlp({
                                                        "question": question,
                                                        "context": context
                                                        })
                                                           """.formatted(modelName, modelName));
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


   public static void main(String[] args) {
      QuestionAnswering z = new QuestionAnswering();
      System.out.println(z.predict("How many months has inflation fallen?",
                                   "Inflation has fallen for the sixth consecutive month."));
   }

   @Value
   public static class QAResult {
      private String answer;
      private double score;
      private int startChar;
      private int endChar;
   }

}
