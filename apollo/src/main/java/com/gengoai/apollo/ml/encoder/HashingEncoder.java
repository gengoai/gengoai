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

import com.gengoai.Validation;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.math.HashingFunctions;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * Uses the hashing trick to reduce the feature space by hashing feature names into a given number of buckets.
 *
 * @author David B. Bracewell
 */
public class HashingEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
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
      if(index >= 0 && index < numberOfFeatures) {
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
      return Collections.emptySet();
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
