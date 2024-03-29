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

import com.gengoai.Validation;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.model.LabelType;
import com.gengoai.apollo.model.sequence.SequenceValidator;
import lombok.NonNull;

public abstract class TFOutputVar extends TFVar {
   private static final long serialVersionUID = 1L;

   protected TFOutputVar(@NonNull String name,
                         @NonNull String servingName,
                         @NonNull Encoder encoder,
                         @NonNull int[] shape) {
      super(name, servingName, encoder, shape);
   }

   public static TFOutputVar classification(String name, String servingName) {
      return new TFClassificationOutput(name, servingName, new IndexEncoder());
   }

   public static TFOutputVar embedding(String name,
                                       String servingName) {
      return new TFOutputVar(name, servingName, NoOptEncoder.INSTANCE, new int[0]) {
         @Override
         public Observation decode(@NonNull NumericNDArray ndArray) {
            return ndArray;
         }

         @Override
         public LabelType getLabelType() {
            return null;
         }
      };
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

   public static TFOutputVar sequence(String name, String servingName, String defaultSymbol) {
      Validation.notNullOrBlank(defaultSymbol, "Default symbol must not be null or blank");
      return new TFSequenceOutputVar(name, servingName, new IndexEncoder(defaultSymbol), SequenceValidator.ALWAYS_TRUE);
   }

   public abstract Observation decode(@NonNull NumericNDArray ndArray);

   public NumericNDArray extractSingleDatumResult(@NonNull NumericNDArray yHat, int index) {
      if (yHat.shape().rank() > 2) {
         return yHat.slice(index);
      }
      return yHat.getAxis(Shape.ROW, index);
   }

   public abstract LabelType getLabelType();

}
