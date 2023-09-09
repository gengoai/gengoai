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

import com.gengoai.Primitives;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class FeatureExtraction {
    public static final String BART_BASE = "facebook/bart-base";
    public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";

    private final String uniqueFuncName;

    public FeatureExtraction(@NonNull String modelName) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1));
    }


    public FeatureExtraction(@NonNull String modelName, int device) {
        this(modelName, modelName, device);
    }

    public FeatureExtraction(@NonNull String modelName,
                             @NonNull String tokenizerName,
                             int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import pipeline\n" +
                                                     uniqueFuncName + "_nlp = pipeline('feature-extraction', model=\"%s\", tokenizer=\"%s\", device=%d,framework=\"pt\")\n" +
                                                     "def " + uniqueFuncName + "(context):\n" +
                                                     "   return " + uniqueFuncName + "_nlp(list(context))\n", modelName, tokenizerName, device));
    }


    public List<NumericNDArray> predict(List<String> texts) {
        List<NumericNDArray> toReturn = new ArrayList<>();
        List<?> list = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, texts));
        for (Object sentenceO : list) {
            List<?> sentenceL = Cast.as(sentenceO);
            NumericNDArray sentenceNDArray = null;
            for (Object wordO : sentenceL) {
                List<?> wordL = Cast.as(wordO);
                for (Object o2 : wordL) {
                    List<Double> vector = Cast.as(o2);
                    double[] array = Primitives.toDoubleArray(vector);
                    if (sentenceNDArray == null) {
                        sentenceNDArray = nd.DFLOAT64.array(array);
                    } else {
                        sentenceNDArray.addi(nd.DFLOAT64.array(array));
                    }
                }
                sentenceNDArray.divi(wordL.size());
                toReturn.add(sentenceNDArray);
            }
        }
        return toReturn;
    }

    public NDArray predict(String texts) {
        return predict(List.of(texts)).get(0);
    }


    public static void main(String[] args) {
        FeatureExtraction te = new FeatureExtraction(BART_BASE, 0);
        System.out.println(te.predict(
                "I am not happy."
                                     ));
    }


}//END OF CLASS FeatureExtraction
