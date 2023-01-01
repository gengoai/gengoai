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

package com.gengoai.apollo.model.tensorflow;

import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.model.embedding.KeyedVectorStore;
import lombok.Getter;
import lombok.NonNull;

public class TFEmbeddingInputVar extends TFInputVar {
   @Getter
   private final KeyedVectorStore embeddings;

   public TFEmbeddingInputVar(@NonNull String name,
                              @NonNull KeyedVectorStore embeddings) {
      super(name, name, NoOptEncoder.INSTANCE, -1, embeddings.dimension());
      this.embeddings = embeddings;
   }


}
