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

package com.gengoai.apollo.math.linalg.compose;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class VectorCompositionTest {
   final NumericNDArray a = nd.DFLOAT32.array(new float[]{1, 2, 3, 4});
   final NumericNDArray b = nd.DFLOAT64.array(new double[]{10, 20, 30, 40});

   @Test
   public void average() {
      assertEquals(nd.DFLOAT32.array(new float[]{5.5f, 11, 16.5f, 22}),
                   VectorCompositions.Average.compose(List.of(a, b)));
   }

   @Test
   public void sum() {
      assertEquals(nd.DFLOAT32.array(new float[]{11, 22, 33, 44}),
                   VectorCompositions.Sum.compose(List.of(a, b)));
   }

   @Test
   public void max() {
      assertEquals(nd.DFLOAT32.array(new double[]{10, 20, 30, 40}),
                   VectorCompositions.Max.compose(List.of(a, b)));
   }


   @Test
   public void min() {
      assertEquals(nd.DFLOAT32.array(new float[]{1, 2, 3, 4}),
                   VectorCompositions.Min.compose(List.of(a, b)));
   }

   @Test
   public void pointWiseMultiply() {
      assertEquals(nd.DFLOAT32.array(new float[]{10, 40, 90, 160}),
                   VectorCompositions.PointWiseMultiply.compose(List.of(a, b)));
   }
}