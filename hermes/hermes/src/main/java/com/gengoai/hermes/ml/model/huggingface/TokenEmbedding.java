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

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.collection.Iterables;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TokenEmbedding {
    public static final String BERT_BASE_UNCASED = "bert-base-uncased";
    public static final String ROBERTA_BASE = "roberta-base";
    public static final String BART_BASE = "facebook/bart-base";
    public static final String BART_LARGE_MNLI = "facebook/bart-large-mnli";

    private final String uniqueFuncName;

    public TokenEmbedding(@NonNull String modelName) {
        this(modelName, modelName, Config.get("gpu.device").asIntegerValue(-1));
    }


    public TokenEmbedding(@NonNull String modelName, int device) {
        this(modelName, modelName, device);
    }

    public TokenEmbedding(@NonNull String modelName,
                          @NonNull String tokenizerName,
                          int device) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(String.format("from transformers import AutoTokenizer, AutoModel, pipeline\n" +
                                                     "import numpy as np\n" +
                                                     "\n" +
                                                     uniqueFuncName + "_tokenizer = AutoTokenizer.from_pretrained(\"%s\")\n" +
                                                     uniqueFuncName + "_pipeline = pipeline(\"feature-extraction\", model=\"%s\", tokenizer=\"%s\", device=%d)\n" +
                                                     "\n" +
                                                     "def " + uniqueFuncName + "(sentence:str,tokens: list):\n" +
                                                     "    inputs = " + uniqueFuncName + "_tokenizer(sentence, return_tensors='pt', truncation=True)\n" +
                                                     "    embeddings = np.asarray(" + uniqueFuncName + "_pipeline(sentence, truncation=True))\n" +
                                                     "\n" +
                                                     "    to_return = []\n" +
                                                     "    for start,end in tokens:\n" +
                                                     "      ts = inputs.char_to_token(start)\n" +
                                                     "      te = inputs.char_to_token(end-1)\n" +
                                                     "      if ts is None or te is None:\n" +
                                                     "        phrase_embedding = np.zeros(embeddings[0][0].shape)\n" +
                                                     "      else:\n" +
                                                     "          phrase_embedding = embeddings[0][ts:te + 1]\n" +
                                                     "          phrase_embedding = np.sum(phrase_embedding, axis=0) / (te + 1 - ts)\n" +
                                                     "      to_return.append(phrase_embedding)\n" +
                                                     "\n" +
                                                     "    return to_return", tokenizerName, modelName, tokenizerName, device));
    }


    private void processHString(HString hString) {
        if (hString.tokenLength() <= 128) {
            List<int[]> offsets = new ArrayList<>();
            for (Annotation token : hString.tokens()) {
                //Offsets need to be relative to the input sentence
                offsets.add(new int[]{token.start() - hString.start(), token.end() - hString.start()});
            }
            List<?> tokenEmbeddings = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, hString.toString(), offsets));
            for (Map.Entry<Annotation, ?> e : Iterables.zip(hString.tokens(), tokenEmbeddings)) {
                Annotation token = e.getKey();
                jep.NDArray<double[]> embedding = Cast.as(e.getValue());
                token.put(Types.EMBEDDING, nd.DFLOAT64.array(embedding.getData()).toFloatArray());
            }
        } else {
            HString left = HString.union(hString.firstToken(),
                                         hString.tokenAt(127));
            processHString(left);
            processHString(HString.union(hString.tokenAt(128),
                                         hString.lastToken()));
        }
    }

    public List<NDArray> embed(String sentence, List<int[]> tokens) {
        List<?> tokenEmbeddings = Cast.as(PythonInterpreter.getInstance().invoke(uniqueFuncName, sentence, tokens));
        return tokenEmbeddings.stream()
                              .map(e -> {
                                  jep.NDArray<double[]> embedding = Cast.as(e);
                                  return nd.DFLOAT64.array(embedding.getData());
                              })
                              .collect(Collectors.toList());
    }

    public void embed(HString hString) {
        for (Annotation sentence : hString.sentences()) {
            processHString(sentence);
        }
    }

    public static void main(String[] args) {
        TokenEmbedding te = new TokenEmbedding(BERT_BASE_UNCASED, 0);
        Document document = Document.create("Now is the time for all good men to come to the aid of their country.");
        document.annotate(Types.TOKEN);
        te.embed(document);
        for (Annotation token : document.tokens()) {
            System.out.println(token + " : " + token.embedding());
        }
    }


}//END OF CLASS TokenEmbedding
