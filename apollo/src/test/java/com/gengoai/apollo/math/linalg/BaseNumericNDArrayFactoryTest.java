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

package com.gengoai.apollo.math.linalg;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public abstract class BaseNumericNDArrayFactoryTest {
   final NumericNDArrayFactory factory;

   protected BaseNumericNDArrayFactoryTest(NumericNDArrayFactory factory) {
      this.factory = factory;
   }


   @Test
   public void vector() {
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0},
            factory.array(new int[]{1, 2, 3}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0},
            factory.array(new long[]{1, 2, 3}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0},
            factory.array(new float[]{1, 2, 3}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0},
            factory.array(new double[]{1, 2, 3}).toDoubleArray(),
            0
      );
   }

   @Test
   public void matrix() {
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new int[][]{{1,4},{2,5},{3,6}}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new long[][]{{1L,4L},{2L,5L},{3L,6L}}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new float[][]{{1,4},{2,5},{3,6}}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new double[][]{{1,4},{2,5},{3,6}}).toDoubleArray(),
            0
      );
   }

   @Test
   public void scalar(){
      Double d = 3d;
      assertEquals(3d, factory.scalar(d).scalarDouble(), 0);
      assertEquals(3d, factory.scalar(3d).scalarDouble(), 0);
   }

   @Test
   public void withShape() {
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(Shape.shape(1,2,3,1), new double[]{1, 2, 3, 4, 5, 6}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(Shape.shape(1,2,3,1), new int[]{1, 2, 3, 4, 5, 6}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(Shape.shape(1,2,3,1), new long[]{1, 2, 3, 4, 5, 6}).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(Shape.shape(1,2,3,1), new float[]{1, 2, 3, 4, 5, 6}).toDoubleArray(),
            0
      );
   }

   @Test
   public void tensor() {
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new int[][][]{
                  {{1}, {2}},
                  {{3}, {4}},
                  {{5}, {6}}
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new long[][][]{
                  {{1}, {2}},
                  {{3}, {4}},
                  {{5}, {6}}
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new float[][][]{
                  {{1}, {2}},
                  {{3}, {4}},
                  {{5}, {6}}
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new double[][][]{
                  {{1}, {2}},
                  {{3}, {4}},
                  {{5}, {6}}
            }).toDoubleArray(),
            0
      );


      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new int[][][][]{
                  { { {1} }, { { 2 } } },
                  { { {3} }, { { 4 } } },
                  { { {5} }, { { 6 } } },
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new long[][][][]{
                  { { {1} }, { { 2 } } },
                  { { {3} }, { { 4 } } },
                  { { {5} }, { { 6 } } },
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new float[][][][]{
                  { { {1} }, { { 2 } } },
                  { { {3} }, { { 4 } } },
                  { { {5} }, { { 6 } } },
            }).toDoubleArray(),
            0
      );
      assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0},
            factory.array(new double[][][][]{
                  { { {1} }, { { 2 } } },
                  { { {3} }, { { 4 } } },
                  { { {5} }, { { 6 } } },
            }).toDoubleArray(),
            0
      );
   }

}
