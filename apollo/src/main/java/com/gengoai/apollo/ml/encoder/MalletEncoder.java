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

package com.gengoai.apollo.ml.encoder;

import cc.mallet.types.Alphabet;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>An {@link Encoder} that wraps a Mallet Alphabet for use with Mallet-based models.</p>
 *
 * @author David B. Bracewell
 */
public class MalletEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   private final Alphabet alphabet;

   /**
    * Instantiates a new MalletEncoder.
    *
    * @param alphabet the Mallet alphabet
    */
   public MalletEncoder(@NonNull Alphabet alphabet) {
      this.alphabet = alphabet;
   }

   @Override
   public String decode(double index) {
      return alphabet.lookupObject((int) index).toString();
   }

   @Override
   public int encode(String variableName) {
      return alphabet.lookupIndex(variableName);
   }

   @Override
   public void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   public Set<String> getAlphabet() {
      return Cast.cast(new HashSet<>(Arrays.asList(alphabet.toArray(new String[0]))));
   }

   @Override
   public boolean isFixed() {
      return true;
   }

   @Override
   public int size() {
      return alphabet.size();
   }
}//END OF MalletEncoder
