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
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Set;

/**
 * <p>An {@link Encoder} for binary values where the true and false are specified. The false label is always encoded as
 * <code>0</code> and the true label as <code>1</code>.</p>
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode
@ToString
public class BinaryEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   @JsonProperty
   private final String trueLabel;
   @JsonProperty
   private final String falseLabel;


   /**
    * Instantiates a new BinaryEncoder with <code>false</code> and <code>true</code>.
    */
   public BinaryEncoder() {
      this("false", "true");
   }


   /**
    * Instantiates a new BinaryEncoder.
    *
    * @param trueLabel  the true label
    * @param falseLabel the false label
    */
   public BinaryEncoder(@NonNull String falseLabel, @NonNull String trueLabel) {
      this.trueLabel = trueLabel;
      this.falseLabel = falseLabel;
   }

   @Override
   public String decode(double index) {
      switch ((int) index) {
         case 0:
            return falseLabel;
         case 1:
            return trueLabel;
      }
      return null;
   }

   @Override
   public int encode(String variableName) {
      if (Strings.safeEquals(variableName, falseLabel, true)) {
         return 0;
      }
      if (Strings.safeEquals(variableName, trueLabel, true)) {
         return 1;
      }
      return -1;
   }

   @Override
   public void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   public Set<String> getAlphabet() {
      return Set.of(falseLabel, trueLabel);
   }

   @Override
   public boolean isFixed() {
      return true;
   }

   @Override
   public int size() {
      return 2;
   }
}//END OF BinaryEncoder
