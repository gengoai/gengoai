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

package com.gengoai.apollo.model.tensorflow;

import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.model.Consts;
import lombok.NonNull;

import java.util.Collections;

import static com.gengoai.apollo.encoder.IndexEncoder.indexEncoder;
import static com.gengoai.apollo.model.Consts.*;

public class TFOneHotInputVar extends TFInputVar {
   public TFOneHotInputVar(@NonNull String name, @NonNull int... shape) {
      super(name, name, indexEncoder(UNKNOWN_WORD, Collections.singletonList(PADDING)), shape);
   }

   public TFOneHotInputVar(@NonNull String name, String servingName, @NonNull int... shape) {
      super(name, servingName, indexEncoder(UNKNOWN_WORD, Collections.singletonList(PADDING)), shape);
   }
}
