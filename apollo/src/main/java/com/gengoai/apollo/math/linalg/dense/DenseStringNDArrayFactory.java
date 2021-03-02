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

package com.gengoai.apollo.math.linalg.dense;

import com.gengoai.apollo.math.linalg.ObjectNDArray;
import com.gengoai.apollo.math.linalg.ObjectNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.NonNull;

public class DenseStringNDArrayFactory extends ObjectNDArrayFactory<String> {

   public DenseStringNDArrayFactory() {
      super(String.class);
   }


   @Override
   public ObjectNDArray<String> array(@NonNull Shape shape, String[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseStringNDArray(shape, a);
   }

   @Override
   public ObjectNDArray<String> array(String[] a) {
      if (a.length == 0) {
         return empty();
      }
      return new DenseStringNDArray(a);
   }

   @Override
   public ObjectNDArray<String> array(String[][] a) {
      if (a.length == 0 || a[0].length == 0) {
         return empty();
      }
      return new DenseStringNDArray(a);
   }

   @Override
   public ObjectNDArray<String> array(String[][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0) {
         return empty();
      }
      return new DenseStringNDArray(a);
   }

   @Override
   public ObjectNDArray<String> array(String[][][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0) {
         return empty();
      }
      return new DenseStringNDArray(a);
   }

   public ObjectNDArray<String> scalar(String o){
      return new DenseStringNDArray(new String[]{o});
   }


   @Override
   public ObjectNDArray<String> zeros(@NonNull Shape shape) {
      return new DenseStringNDArray(shape);
   }
}
