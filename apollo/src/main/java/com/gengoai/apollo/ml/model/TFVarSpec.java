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

package com.gengoai.apollo.ml.model;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.ml.encoder.Encoder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TFVarSpec implements Serializable {
   @NonNull String servingName;
   @NonNull Encoder encoder;
   @NonNull int[] shape;

   public static TFVarSpec varSpec(String servingName, @NonNull Encoder encoder, @NonNull int... shape) {
      return new TFVarSpec(Validation.notNullOrBlank(servingName),
                           encoder,
                           shape);
   }

   public NDArray createBatchNDArray(int batch_size, @NonNull int... dimensions) {
      int[] batch_shape = new int[shape.length + 1];
      batch_shape[0] = batch_size;
      for (int i = 0; i < shape.length; i++) {
         if (shape[i] < 0) {
            batch_shape[i + 1] = dimensions[i];
         } else {
            batch_shape[i + 1] = shape[i];
         }
      }
      return NDArrayFactory.ND.array(batch_shape);
   }

   public Tensor<?> toTensor(NDArray n) {
      if (shape.length == 1) {
         return Tensor.create(n.toFloatArray2());
      }
      return Tensor.create(n.toFloatArray3());
   }

   public void updateBatch(NDArray batch, int index, NDArray x) {
      Shape shape = batch.shape();
      if (batch.shape().channels() > 0) {
         batch.setSlice(index, x.padPost(shape.rows(), shape.columns()));
      } else {
         batch.setRow(index, x.padRowPost(shape.columns()).T());
      }
   }

}//END OF TFVarSpec
