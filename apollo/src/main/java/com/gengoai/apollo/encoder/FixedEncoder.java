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

package com.gengoai.apollo.encoder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.collection.Iterables;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * <p>Implementation of a {@link Encoder} for a Fixed vocabulary.</p>
 *
 * @author David B. Bracewell
 */
public class FixedEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   @JsonProperty("alphabet")
   private final Index<String> alphabet = new HashMapIndex<>();
   @JsonProperty("unknown")
   private final String unknown;

   /**
    * Instantiates a new FixedEncoder.
    *
    * @param alphabet the alphabet
    * @param unknown  the unknown
    */
   @JsonCreator
   public FixedEncoder(@JsonProperty("alphabet") @NonNull Iterable<String> alphabet,
                       @JsonProperty("unknown") String unknown) {
      this.alphabet.addAll(alphabet);
      this.unknown = Strings.emptyToNull(unknown);
   }

   /**
    * Instantiates a new FixedEncoder reading the vocabulary from the given file.
    *
    * @param vocabFile the vocab file with one entry per line
    * @param unknown   the vocabulary item representing an unknown value
    */
   public FixedEncoder(@NonNull Resource vocabFile, String unknown) {
      try {
         this.alphabet.addAll(Iterables.filter(Iterables.transform(vocabFile.readLines(), String::strip),
                                               Strings::isNotNullOrBlank));
         this.unknown = Strings.emptyToNull(unknown);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Instantiates a new FixedEncoder.
    *
    * @param alphabet the alphabet
    */
   public FixedEncoder(@NonNull Iterable<String> alphabet) {
      this(alphabet, null);
   }

   /**
    * Static constructor for creating a FixedEncoder from a vocabulary in the given <code>vocabFile</code> and having
    * the given <code>unknown</code> word.
    *
    * @param vocabFile the vocab file with one entry per line
    * @param unknown   the vocabulary item representing an unknown value
    * @return the FixedEncoder
    */
   public static FixedEncoder fixedEncoder(@NonNull Resource vocabFile, String unknown) {
      return new FixedEncoder(vocabFile, unknown);
   }

   @Override
   public String decode(double index) {
      return alphabet.get((int) index);
   }

   @Override
   public int encode(String variableName) {
      int index = alphabet.getId(variableName);
      if (index == -1 && unknown != null) {
         index = alphabet.getId(unknown);
      }
      return index;
   }

   @Override
   public void fit(@NonNull MStream<Observation> stream) {
   }

   @Override
   public Set<String> getAlphabet() {
      return Collections.unmodifiableSet(alphabet.itemSet());
   }

   @Override
   public boolean isFixed() {
      return true;
   }

   @Override
   public int size() {
      return alphabet.size();
   }
}//END OF FixedEncoder
