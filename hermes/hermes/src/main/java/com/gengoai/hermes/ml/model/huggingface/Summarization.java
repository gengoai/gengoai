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
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.summarization.Summarizer;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Summarization implements Summarizer {
    public static final String FLAN_T5_BASE_SAMSUM = "philschmid/flan-t5-base-samsum";

    private final String uniqueFuncName;

    public Summarization(@NonNull String modelName,
                         int maxLength) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1), maxLength);
    }

    public Summarization(@NonNull String modelName,
                         int device,
                         int maxLength) {
        this(modelName, modelName, device, maxLength);
    }

    public Summarization(@NonNull String modelName,
                         @NonNull String tokenizerName,
                         int device,
                         int maxLength) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import pipeline\n" +
                                                     uniqueFuncName + "_nlp = pipeline('summarization', model=\"%s\", tokenizer=\"%s\", device=%d)\n" +
                                                     "def " + uniqueFuncName + "(context):\n" +
                                                     "   return " + uniqueFuncName + "_nlp(list(context), max_length=%d)\n", modelName, tokenizerName, device, maxLength));
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
        List<Map<String, ?>> rvals = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, texts));
        return rvals.stream().map(rList -> rList.get("summary_text").toString()).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Summarization summarization = new Summarization(FLAN_T5_BASE_SAMSUM, 128);
        System.out.println(summarization.predict("The US has " +
                                                         "passed the peak on new coronavirus cases, " +
                                                         "President Donald Trump said and predicted that " +
                                                         "some states would reopen this month. The US has " +
                                                         "over 637,000 confirmed Covid-19 cases and over " +
                                                         "30,826 deaths, the highest for any country in " +
                                                         "the world. At the daily White House briefing on " +
                                                         "Wednesday, Trump said new guidelines to reopen " +
                                                         "the country would be announced on Thursday after " +
                                                         "he speaks to governors. \"We'll be the comeback " +
                                                         "kids, all of us,\" he said. \"We want to get our " +
                                                         "country back.\" The Trump administration has " +
                                                         "earlier given guidelines for a staged reopening " +
                                                         "of the economy, with a shutdown of schools and " +
                                                         "non-essential businesses. Trump said some states " +
                                                         "could reopen even before the May 1 deadline for " +
                                                         "the current shutdown to expire. \"I think some " +
                                                         "of the states can actually open up before the " +
                                                         "deadline of May 1,\" he said. \"I think that " +
                                                         "will happen.\" Trump said he would speak to " +
                                                         "governors on Thursday, and anticipated \"a lot " +
                                                         "of questions on what we're talking about\". \"We " +
                                                         "are going to have a very interesting talk,\" he " +
                                                         "said. \"I think you're going to have a lot of " +
                                                         "happy people.\" Trump said the US had \"passed " +
                                                         "the peak on new cases\". \"Hopefully that will " +
                                                         "continue, and we will continue to make great " +
                                                         "progress,\" he said. \"These encouraging results " +
                                                         "have put us in a very strong position to finalize " +
                                                         "guidelines for states on reopening the country, " +
                                                         "which we'll be announcing ... tomorrow.\" Trump " +
                                                         "said the US had conducted 3.3 million coronavirus " +
                                                         "tests, and would soon be conducting 2 million " +
                                                         "tests a week."));
    }
}//END OF Summarization
