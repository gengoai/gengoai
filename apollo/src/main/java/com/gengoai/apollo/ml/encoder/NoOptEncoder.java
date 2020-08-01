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

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * <p>An {@link Encoder} implementation that always returns <code>-1</code> on encode and <code>null</code> on
 * decode</p>
 */
public class NoOptEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   /**
    * Singleton instance of the NoOptEncoder
    */
   public static final Encoder INSTANCE = new NoOptEncoder();

   private NoOptEncoder() {

   }

   @Override
   public String decode(double index) {
      return null;
   }

   @Override
   public int encode(String variableName) {
      return -1;
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
      return 0;
   }
}//END OF NoOptEncoder
