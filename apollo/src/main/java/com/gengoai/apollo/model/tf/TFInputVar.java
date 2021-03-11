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

package com.gengoai.apollo.model.tf;

import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.encoder.NoOptEncoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TFInputVar extends TFVar {
   private static final long serialVersionUID = 1L;

   protected TFInputVar(@NonNull String name,
                        @NonNull Encoder encoder,
                        @NonNull int... shape) {
      super(name, name, encoder, shape);
   }

   protected TFInputVar(@NonNull String name,
                        @NonNull String servingName,
                        @NonNull Encoder encoder,
                        @NonNull int... shape) {
      super(name, servingName, encoder, shape);
   }

   public static TFInputVar sequence(String name) {
      return new TFInputVar(name, new IndexEncoder("--PAD--"), -1);
   }

   public static TFInputVar sequence(String name, int... shape) {
      return new TFInputVar(name, new IndexEncoder("--PAD--"), shape);
   }

   public static TFInputVar sequence(String name, Encoder encoder, int... shape) {
      return new TFInputVar(name, encoder, shape);
   }

   public static TFInputVar var(String name, String servingName, Encoder encoder, int... shape) {
      return new TFInputVar(name, servingName, encoder, shape);
   }

   public static TFInputVar sequence(String name, Encoder encoder) {
      return new TFInputVar(name, encoder, -1);
   }

   public static TFInputVar featureVector(String name, int... shape) {
      return new TFInputVar(name, NoOptEncoder.INSTANCE, shape);
   }


}
