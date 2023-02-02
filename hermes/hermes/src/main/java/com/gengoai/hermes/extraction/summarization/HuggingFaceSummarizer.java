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

package com.gengoai.hermes.extraction.summarization;

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.python.PythonInterpreter;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public class HuggingFaceSummarizer implements Summarizer {
   public static final String BART_LARGE_CNN_SAMSUM = "philschmid/flan-t5-base-samsum";

   public HuggingFaceSummarizer() {
      this(BART_LARGE_CNN_SAMSUM, 100);
   }

   public HuggingFaceSummarizer(int maxLength) {
      this(BART_LARGE_CNN_SAMSUM, maxLength);
   }

   private final PythonInterpreter interpreter;

   public HuggingFaceSummarizer(@NonNull String modelName, int maxLength) {
      Validation.checkArgument(maxLength > 0);
      Validation.checkArgument(Strings.isNotNullOrBlank(modelName));
      this.interpreter = new PythonInterpreter("""
                                                     from transformers import pipeline

                                                     nlp = pipeline('summarization', model="%s")
                                                                                                                         
                                                     def pipe(context):
                                                        return nlp(context, max_length=%d)
                                                           """.formatted(modelName, maxLength));
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

   public static void main(String[] args) {
      HuggingFaceSummarizer summarizer = new HuggingFaceSummarizer(50);
      System.out.println(summarizer.extract(Document.create("Jeff: Can I train a \uD83E\uDD17 Transformers model on Amazon SageMaker? \n" +
                                                            "Philipp: Sure you can use the new Hugging Face Deep Learning Container. \n" +
                                                            "Jeff: ok.\n" +
                                                            "Jeff: and how can I get started? \n" +
                                                            "Jeff: where can I find documentation? \n" +
                                                            "Philipp: ok, ok you can find everything here. https://huggingface.co/blog/the-partnership-amazon-sagemaker-and-hugging-face")));
   }
}
