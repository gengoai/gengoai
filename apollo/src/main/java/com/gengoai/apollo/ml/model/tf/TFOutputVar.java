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

package com.gengoai.apollo.ml.model.tf;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;
import com.gengoai.apollo.ml.observation.Observation;
import lombok.NonNull;

public abstract class TFOutputVar extends TFVar {
   private static final long serialVersionUID = 1L;

   protected TFOutputVar(@NonNull String name,
                         @NonNull String servingName,
                         @NonNull Encoder encoder,
                         @NonNull int[] shape) {
      super(name, servingName, encoder, shape);
   }

   public static TFOutputVar sequence(String name, String servingName, String defaultSymbol) {
      Validation.notNullOrBlank(defaultSymbol, "Default symbol must not be null or blank");
      return new TFSequenceOutputVar(name, servingName, new IndexEncoder(defaultSymbol), SequenceValidator.ALWAYS_TRUE);
   }

   public static TFOutputVar sequence(String name, String servingName, Encoder encoder) {
      return new TFSequenceOutputVar(name, servingName, encoder, SequenceValidator.ALWAYS_TRUE);
   }

   public static TFOutputVar sequence(String name, String servingName, String defaultSymbol, SequenceValidator validator) {
      Validation.notNullOrBlank(defaultSymbol, "Default symbol must not be null or blank");
      return new TFSequenceOutputVar(name, servingName, new IndexEncoder(defaultSymbol), validator);
   }

   public static TFOutputVar sequence(String name, String servingName, Encoder encoder, SequenceValidator validator) {
      return new TFSequenceOutputVar(name, servingName, encoder, validator);
   }

   public static TFOutputVar classification(String name, String servingName){
      return new TFClassificationOutput(name,servingName,new IndexEncoder());
   }

   public NDArray extractSingleDatumResult(@NonNull NDArray yHat, int index){
      if (yHat.shape().order() > 2) {
         return yHat.slice(index);
      }
      return yHat.getRow(index);
   }
   public abstract Observation decode(@NonNull NDArray ndArray);

   public abstract LabelType getLabelType();

}
