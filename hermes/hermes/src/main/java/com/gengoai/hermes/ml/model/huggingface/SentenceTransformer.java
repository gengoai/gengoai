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

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SentenceTransformer {
    public static final String ALL_MINILM_L6_V2 = "sentence-transformers/all-MiniLM-L6-v2";
    private final String uniqueFuncName;


    public SentenceTransformer(String modelName) {
        this(modelName, Config.get("gpu.device").asIntegerValue(0));
    }

    public SentenceTransformer(String modelName,
                               int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from sentence_transformers import SentenceTransformer\n" +
                                                     uniqueFuncName + "_nlp=SentenceTransformer('%s', device=%d)\n" +
                                                     "def " + uniqueFuncName + "(context):\n" +
                                                     "   return " + uniqueFuncName + "_nlp.encode(list(context))\n", modelName, device)

                              );
    }

    public NumericNDArray embed(String sentence) {
        return embed(List.of(sentence)).get(0);
    }

    public List<NumericNDArray> embed(List<String> sentences) {
        List<NumericNDArray> toReturn = new ArrayList<>();
        jep.NDArray<float[]> ndArray = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, sentences));
        int vectorDim = ndArray.getDimensions()[1];
        float[] data = ndArray.getData();
        for (int i = 0; i < sentences.size(); i++) {
            float[] vector = Arrays.copyOfRange(data, i * vectorDim, i * vectorDim + vectorDim);
            toReturn.add(nd.DFLOAT32.array(vector));
        }
        return toReturn;
    }

    public void embed(@NonNull HString hString) {
        List<String> sentences = new ArrayList<>();
        List<Annotation> sentenceHString = new ArrayList<>();
        for (Annotation sentence : hString.sentences()) {
            sentences.add(sentence.toString());
            sentenceHString.add(sentence);
        }
        jep.NDArray<float[]> ndArray = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, sentences));
        int vectorDim = ndArray.getDimensions()[1];
        float[] data = ndArray.getData();
        for (int i = 0; i < sentenceHString.size(); i++) {
            Annotation sentence = sentenceHString.get(i);
            float[] vector = Arrays.copyOfRange(data, i * vectorDim, i * vectorDim + vectorDim);
            sentence.put(Types.EMBEDDING, nd.DFLOAT32.array(vector).toFloatArray());
        }
    }

    public static void main(String[] args) {
        SentenceTransformer sentenceTransformer = new SentenceTransformer(SentenceTransformer.ALL_MINILM_L6_V2);
        System.out.println(sentenceTransformer.embed("This is a test."));
    }

}//END OF SentenceTransformer
