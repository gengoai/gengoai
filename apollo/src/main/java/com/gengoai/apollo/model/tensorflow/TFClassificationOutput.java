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

import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Classification;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.model.LabelType;
import lombok.NonNull;

import java.util.List;

public class TFClassificationOutput extends TFOutputVar {
   private static final long serialVersionUID = 1L;

   protected TFClassificationOutput(@NonNull String name,
                                    @NonNull String servingName,
                                    @NonNull Encoder encoder) {
      super(name, servingName, encoder, new int[]{-1});
   }

   @Override
   public Observation decode(@NonNull NumericNDArray ndArray) {
      return new Classification(ndArray, getEncoder());
   }

   @Override
   protected int[] dimensionsOf(List<Datum> dataSet) {
      return new int[]{getEncoder().size()};
   }

   @Override
   public LabelType getLabelType() {
      return LabelType.classificationType(getEncoder().size());
   }
}
