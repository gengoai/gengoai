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

package com.gengoai.hermes.en;

import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.hermes.ml.model.TFSentenceEmbeddingProjector;

import java.util.List;

public class ENSentenceEncoder extends TFSentenceEmbeddingProjector {
    public static final int DIMENSION = 300;
    private static final long serialVersionUID = 1234567L;

    public ENSentenceEncoder() {
        super(List.of(TFInputVar.embedding(TOKENS, ENResources.gloveLargeEmbeddings())),
              "sentence",
              "encoder_lstm/add_16");
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

}
