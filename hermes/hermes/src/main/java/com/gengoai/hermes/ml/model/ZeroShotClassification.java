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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

public class ZeroShotClassification {
   public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";

   private final PythonInterpreter interpreter;


   public ZeroShotClassification() {
      this(BART_LARGE_MNLI, -1);
   }

   public ZeroShotClassification(@NonNull String modelName, int device) {
      this.interpreter = new PythonInterpreter("""
                                                     from transformers import pipeline
                                                     classifier = pipeline("zero-shot-classification", model="%s", device=%d)
                                                                       
                                                     def pipe(sentence,labels):
                                                        return classifier(sentence,labels)
                                                     """.formatted(modelName,device));
   }

   public List<Tuple2<String, Double>> predict(String context, @NonNull String... labels) {
      return predict(context, Arrays.asList(labels));
   }


   public List<Tuple2<String, Double>> predict(String context, @NonNull List<String> labels) {
      HashMap<String, ?> m = Cast.as(interpreter.invoke("pipe", context, labels));
      List<String> lbls = Cast.as(m.get("labels"));
      List<Double> scores = Cast.as(m.get("scores"));
      return Streams.asStream(Iterables.zip(lbls, scores))
                    .map(e -> $(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
   }


   public static void main(String[] args) {
      ZeroShotClassification z = new ZeroShotClassification(BART_LARGE_MNLI, 0);
      System.out.println(z.predict("Inflation has fallen for the sixth consecutive month.",
                                   "Economics", "Business", "Politics"));
   }


}
