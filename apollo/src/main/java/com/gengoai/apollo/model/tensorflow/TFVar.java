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
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.*;
import org.tensorflow.Tensor;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Describes a variable input or output in a TensorFlow model including the observation name, the TensorFlow serving
 * model name, the shape, and associated encoder.</p>
 *
 * @author David B. Bracewell
 */
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class TFVar implements Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final String name;
   @NonNull
   private final String servingName;
   @NonNull
   private final int[] shape;
   @NonNull
   @Setter
   private Encoder encoder;

   /**
    * Instantiates a new TFVar.
    *
    * @param name        the Observation name
    * @param servingName the serving name
    * @param encoder     the encoder
    * @param shape       the shape
    */
   public TFVar(@NonNull String name,
                @NonNull String servingName,
                @NonNull Encoder encoder,
                @NonNull int[] shape) {
      Validation.notNullOrBlank(name, "Name cannot be null or blank");
      Validation.notNullOrBlank(servingName, "servingName cannot be null or blank");
      this.name = name;
      this.servingName = servingName;
      this.shape = shape;
      this.encoder = encoder;
   }


   protected int[] dimensionsOf(List<Datum> dataSet) {
      int[] dimensions = new int[shape.length];
      for (int i = 0; i < dimensions.length; i++) {
         if (shape[i] < 0) {
            if (i == 0) {
               dimensions[i] = (int) dataSet.stream()
                                            .mapToDouble(d -> d.get(getName()).asNDArray().shape().rows())
                                            .max()
                                            .orElse(0d);
            } else if (i == 1) {
               dimensions[i] = (int) dataSet.stream()
                                            .mapToDouble(d -> d.get(getName()).asNDArray().shape().columns())
                                            .max()
                                            .orElse(0d);
            } else if (i == 2) {
               dimensions[i] = (int) dataSet.stream()
                                            .mapToDouble(d -> d.get(getName()).asNDArray().shape().channels())
                                            .max()
                                            .orElse(0d);
            } else {
               dimensions[i] = (int) dataSet.stream()
                                            .mapToDouble(d -> d.get(getName()).asNDArray().shape().kernels())
                                            .max()
                                            .orElse(0d);
            }
         } else {
            dimensions[i] = shape[i];
         }
      }
      return dimensions;
   }

   /**
    * <p>Converts the list of Datum into a single TensorFlow Tensor</p>
    *
    * @param data the data to convert
    * @return the tensor
    */
   public final Tensor toTensor(@NonNull List<Datum> data) {
      boolean isScalar = data.get(0).get(name).asNDArray().shape().isScalar();
      int[] batch_shape;

      if (isScalar) {
         batch_shape = new int[]{data.size()};
      } else {
         batch_shape = new int[shape.length + 1];
         batch_shape[0] = data.size();
         System.arraycopy(dimensionsOf(data), 0, batch_shape, 1, shape.length);
      }


      //Create the batch NDArray
      NDArray batch = data.get(0).get(name).asNDArray().factory().zeros(batch_shape);
      Object padValue = batch.getType() == String.class ? "--PAD--" : 0;

      for (int i = 0; i < data.size(); i++) {
         NDArray ni = data.get(i).get(name).asNDArray();
         if (batch.shape().isScalar()){
            batch.set(i, ni.scalar());
         } else if (batch.shape().channels() > 0) {
            batch.setSlice(i, ni.padPostWith(padValue,
                                             Shape.ROW, batch.rows(),
                                             Shape.COLUMN, batch.columns()));
         } else {
            batch.setAxis(Shape.ROW, i, ni.padPostWith(padValue,
                                                       Shape.ROW, batch.columns()).T());
         }
      }
      return batch.toTensor();
   }


}//END OF TFVar
