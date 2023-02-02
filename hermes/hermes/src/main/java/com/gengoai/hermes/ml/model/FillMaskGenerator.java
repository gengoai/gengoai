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

import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import com.gengoai.tuple.Tuple;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

public class FillMaskGenerator {
   public static final String BERT_BASE_UNCASED = "bert-base-uncased";
   public static final String BERT_MASK_TOKEN = "[MASK]";
   public static final String XLM_ROBERTA_LARGE = "xlm-roberta-large";
   public static final String ROBERTA_MASK_TOKEN = "<mask>";

   private final PythonInterpreter interpreter;


   public FillMaskGenerator() {
      this(XLM_ROBERTA_LARGE);
   }

   public FillMaskGenerator(@NonNull String modelName) {
      this.interpreter = new PythonInterpreter("""
                                                     from transformers import pipeline

                                                     nlp = pipeline('fill-mask', model="%s")
                                                                                                                         
                                                     def pipe(context):
                                                        return nlp(context)
                                                           """.formatted(modelName, modelName));
   }

   public List<Tuple2<String, Double>> predict(String context) {
      List<?> m = Cast.as(interpreter.invoke("pipe", context));
      List<Tuple2<String, Double>> rval = new ArrayList<>();
      for (Object o : m) {
         Map<String, ?> map = Cast.as(o);
         String tokenStr = map.get("token_str").toString();
         double score = ((Number) map.get("score")).doubleValue();
         rval.add($(tokenStr, score));
      }
      return rval;
   }

   public static void main(String[] args) {
      FillMaskGenerator generator = new FillMaskGenerator();
      System.out.println(generator.predict("John married Mary last june and had twins. John values %s.".formatted(ROBERTA_MASK_TOKEN)));
   }


}
