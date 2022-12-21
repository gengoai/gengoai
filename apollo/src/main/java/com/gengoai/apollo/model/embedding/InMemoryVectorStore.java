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

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * <p>An in-memory implementation of a {@link KeyedVectorStore}.</p>
 *
 * @author David B. Bracewell
 */
public class InMemoryVectorStore implements KeyedVectorStore {
   private static final long serialVersionUID = 1L;
   protected final Map<String, NumericNDArray> vectors = new ConcurrentHashMap<>();
   @NonNull
   @Getter
   private final String unknownKey;
   @NonNull
   @Getter
   private final String[] specialKeys;
   protected int dimension;

   public InMemoryVectorStore(int dimension) {
      this(dimension, null, null);
   }

   public InMemoryVectorStore(int dimension, String unknownKey, String[] specialKeys) {
      this.dimension = dimension;
      this.unknownKey = Strings.nullToEmpty(unknownKey);
      this.specialKeys = specialKeys == null
            ? new String[0]
            : specialKeys;
      for (String specialKey : this.specialKeys) {
         vectors.put(specialKey, nd.DFLOAT32.zeros(dimension));
      }
      if (Strings.isNotNullOrBlank(unknownKey)) {
         vectors.put(unknownKey, nd.DFLOAT32.zeros(dimension));
      }
   }

   @Override
   public int dimension() {
      return dimension;
   }

   @Override
   public Set<String> getAlphabet() {
      return vectors.keySet();
   }

   @Override
   public NumericNDArray getVector(@NonNull String key) {
      return vectors.getOrDefault(key, nd.DFLOAT32.zeros(dimension).setLabel(key));
   }

   @Override
   public int size() {
      return vectors.size();
   }

   @Override
   public Stream<NumericNDArray> stream() {
      return vectors.values().stream();
   }

   @Override
   public void updateVector(String word, @NonNull NumericNDArray vector) {
      vectors.put(word, vector);
   }
}//END OF InMemoryVectorStore
