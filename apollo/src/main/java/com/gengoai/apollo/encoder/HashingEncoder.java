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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Validation;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.math.HashingFunctions;
import com.gengoai.stream.MStream;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Uses the hashing trick to reduce the feature space by hashing feature names into a given number of buckets.
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class HashingEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   @JsonProperty
   private final int numberOfFeatures;

   /**
    * Instantiates a new HashingEncoder.
    *
    * @param numberOfFeatures the number of features to represent
    */
   public HashingEncoder(int numberOfFeatures) {
      Validation.checkArgument(numberOfFeatures > 0, "Number of features should be > 0");
      this.numberOfFeatures = numberOfFeatures;
   }

   @Override
   public String decode(double index) {
      if (index >= 0 && index < numberOfFeatures) {
         return "Hash(" + (int) index + ")";
      }
      return null;
   }

   @Override
   public int encode(String variableName) {
      int out = HashingFunctions.hash32x86(variableName.getBytes()) % numberOfFeatures;
      return out >= 0
            ? out
            : out + numberOfFeatures;
   }

   @Override
   public void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   public Set<String> getAlphabet() {
      return IntStream.range(0, size())
                      .mapToObj(index -> "Hash(" + index + ")")
                      .collect(Collectors.toSet());
   }

   @Override
   public boolean isFixed() {
      return true;
   }

   @Override
   public int size() {
      return numberOfFeatures;
   }

}//END OF HashingEncoder
