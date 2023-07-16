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
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.conversion.Cast;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FeatureExtraction {
    public static final String BART_BASE = "facebook/bart-base";

    private final PythonInterpreter interpreter;


    public FeatureExtraction(@NonNull String modelName, int device) {
        this(modelName, modelName, device);
    }

    public FeatureExtraction(@NonNull String modelName,
                             @NonNull String tokenizerName,
                             int device) {
        this.interpreter = new PythonInterpreter("""
                from transformers import pipeline

                nlp = pipeline('feature-extraction', model="%s", tokenizer="%s", device=%d,framework="pt")
                                                                                    
                def pipe(context):
                   return nlp(list(context))
                      """.formatted(modelName, tokenizerName, device));
    }


    public List<NumericNDArray> predict(List<String> texts) {
        List<NumericNDArray> toReturn = new ArrayList<>();

        List<?> list = Cast.as(interpreter.invoke("pipe", texts));
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

}//END OF CLASS FeatureExtraction
