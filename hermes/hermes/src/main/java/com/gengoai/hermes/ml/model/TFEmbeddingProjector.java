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

import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.ml.HStringMLModel;
import lombok.NonNull;

import java.util.List;

public abstract class TFEmbeddingProjector extends TFModel implements HStringMLModel {

    public TFEmbeddingProjector(@NonNull List<TFInputVar> inputVars,
                                String outputName,
                                String outputServingName) {
        super(inputVars, List.of(TFOutputVar.embedding(outputName, outputServingName)));
    }

    @Override
    public Model delegate() {
        return this;
    }

    @Override
    public void setVersion(String version) {
        throw new UnsupportedOperationException();
    }


}
