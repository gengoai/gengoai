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

package com.gengoai.apollo.data.transform.vectorizer;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Sequence;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.embedding.InMemoryVectorStore;
import com.gengoai.apollo.model.embedding.KeyedVectorStore;
import com.gengoai.apollo.model.embedding.VSTextUtils;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingVectorizer extends Vectorizer<EmbeddingVectorizer> {

   public EmbeddingVectorizer(KeyedVectorStore embedding) {
      super(NoOptEncoder.INSTANCE);
   }

   @Override
   protected NumericNDArray transform(@NonNull Observation observation) {
      KeyedVectorStore embedding = Cast.as(encoder);
      if (observation instanceof Variable) {

         NumericNDArray n = embedding.getVector(((Variable) observation).getName());
         return n;
      } else if (observation instanceof Sequence) {
         Sequence<? extends Observation> sequence = Cast.as(observation);
         List<NumericNDArray> rows = new ArrayList<>();
         sequence.forEach(o -> rows.add(transform(o)));
         return nd.vstack(Cast.cast(rows));
      }
      throw new IllegalArgumentException("Unsupported Observation: " + observation.getClass());
   }

   protected void updateMetadata(@NonNull DataSet dataset) {
      KeyedVectorStore embedding = Cast.as(encoder);
      dataset.updateMetadata(output, m -> {
         m.setDimension(embedding.dimension());
         m.setType(NDArray.class);
         m.setEncoder(encoder);
      });
   }


}
