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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.collection.Lists;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>An in-memory implementation of a {@link KeyedVectorStore}.</p>
 *
 * @author David B. Bracewell
 */
public class InMemoryVectorStore implements KeyedVectorStore {
   private static final long serialVersionUID = 1L;
   protected final Index<String> alphabet = new HashMapIndex<>();
   protected final List<NDArray> vectors = new ArrayList<>();
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
      if(this.specialKeys.length > 0) {
         alphabet.addAll(Arrays.asList(this.specialKeys));
         for(String specialKey : this.specialKeys) {
            vectors.add(NDArrayFactory.ND.array(dimension));
         }
      }
      if(Strings.isNotNullOrBlank(unknownKey)) {
         alphabet.add(unknownKey);
         vectors.add(NDArrayFactory.ND.array(dimension));
      }
   }

   @Override
   public int addOrGetIndex(@NonNull String key) {
      return alphabet.add(key);
   }

   @Override
   public String decode(double index) {
      return alphabet.get((int) index);
   }

   @Override
   public int dimension() {
      return dimension;
   }

   @Override
   public int encode(String variableName) {
      return alphabet.getId(variableName);
   }

   @Override
   public void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   public Set<String> getAlphabet() {
      return alphabet.itemSet();
   }

   @Override
   public NDArray getVector(@NonNull String key) {
      int index = alphabet.getId(key);
      return index >= 0
             ? vectors.get(index)
             : NDArrayFactory.ND.array(1, dimension);
   }

   @Override
   public boolean isFixed() {
      return true;
   }

   @Override
   public int size() {
      return alphabet.size();
   }

   @Override
   public Stream<NDArray> stream() {
      return vectors.stream();
   }

   @Override
   public void updateVector(int index, @NonNull NDArray vector) {
      if(index < 0) {
         throw new IndexOutOfBoundsException();
      }
      Lists.ensureSize(vectors, index + 1, null);
      vectors.set(index, vector);
   }
}//END OF InMemoryVectorStore
