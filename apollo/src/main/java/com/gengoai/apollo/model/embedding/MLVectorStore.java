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

package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.math.measure.Similarity;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * An {@link Encoder} in which the encoded index has an {@link NDArray} associated with it for embedding the encoded
 * String into a vector. Each vector store can have an "unknown key" which when defined will be used in place of keys
 * that are not found in the alphabet. Additionally, a set of "special keys" are defined that will always be added to
 * the vector store as an array of all zero values.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class MLVectorStore implements VectorStore {


   /**
    * @return the array of special tokens
    */
   public abstract String[] getSpecialKeys();

   /**
    * The key representing that is used when other keys are not able to be encoded.
    *
    * @return the unknown key
    */
   public abstract String getUnknownKey();

   @Override
   public NumericNDArray getVector(@NonNull String id) {
      if (containsKey(id)) {
         return getVectorImpl(id);
      }
      if (Strings.isNotNullOrBlank(getUnknownKey())) {
         return getVectorImpl(getUnknownKey());
      }
      return nd.DFLOAT32.zeros(dimension()).setLabel(id);
   }


   @Override
   public List<VSQueryResult> query(@NonNull NumericNDArray vector, int K) {
      if (K <= 0) {
         return Collections.emptyList();
      }
      return stream().parallel()
                     .map(n -> new VSQueryResult(n.getLabel().toString(), Similarity.Cosine.calculate(n, vector)))
                     .sorted(Comparator.reverseOrder())
                     .limit(K)
                     .toList();
   }

   @Override
   public boolean putAllVectors(@NonNull Map<String, NumericNDArray> vectors) {
      return vectors.entrySet()
                    .stream()
                    .allMatch(e -> putVector(e.getKey(), e.getValue()));
   }

   protected abstract NumericNDArray getVectorImpl(String id);

}//END OF KeyedVectorStore
