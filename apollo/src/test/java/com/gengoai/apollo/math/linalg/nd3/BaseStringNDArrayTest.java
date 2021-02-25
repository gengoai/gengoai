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

package com.gengoai.apollo.math.linalg.nd3;

import org.junit.Test;

import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public abstract class BaseStringNDArrayTest {

   final NDArray<String> matrix34;
   final NDArray<String> matrix43;
   final NDArray<String> tensor2234;
   final NDArray<String> tensor234;
   final NDArray<String> rowVector;
   final NDArray<String> colVector;
   final NDArray<String> scalar;
   final NDArrayFactory<String> factory;

   public BaseStringNDArrayTest(NDArrayFactory<String> factory) {
      this.factory = factory;
      this.matrix34 = factory.array(new String[][]{
            {"A", "B", "C", "D"},
            {"E", "F", "G", "H"},
            {"I", "J", "K", "L"}
      });
      this.matrix43 = factory.array(new String[][]{
            {"A", "B", "C"},
            {"D", "E", "F"},
            {"G", "H", "I"},
            {"J", "K", "L"}
      });
      this.rowVector = factory.array(new String[]{"1", "2", "3", "4"});
      this.colVector = factory.arange(Shape.shape(4, 1));
      this.tensor234 = factory.rand(Shape.shape(2, 3, 4), supplier());
      this.tensor2234 = factory.rand(Shape.shape(2, 3, 4), supplier());
      this.scalar = factory.scalar("TEST");
   }

   @Test
   public void fillIf(){
      assertEquals(factory.scalar("TEST"), factory.zeros(1).fillIf(Objects::isNull, "TEST"));
   }

   public static Supplier<String> supplier() {
      return new Supplier<String>() {
         char c = 'A';

         @Override
         public String get() {
            String out = Character.toString(c);
            c++;
            return out;
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void addAxisNDArrayFail() {
      scalar.add(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addAxisPositionNDArrayFail() {
      scalar.add(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addAxisPositionScalarFail() {
      scalar.add(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addNDArrayFail() {
      scalar.add(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void addScalarFail() {
      scalar.add(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addiAxisNDArrayFail() {
      scalar.addi(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addiAxisPositionNDArrayFail() {
      scalar.addi(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addiAxisPositionScalarFail() {
      scalar.addi(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addiNDArrayFail() {
      scalar.addi(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void addiScalarFail() {
      scalar.addi(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void divAxisNDArrayFail() {
      scalar.div(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void divAxisPositionNDArrayFail() {
      scalar.div(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void divAxisPositionScalarFail() {
      scalar.div(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void divNDArrayFail() {
      scalar.div(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void divScalarFail() {
      scalar.div(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void diviAxisNDArrayFail() {
      scalar.divi(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void diviAxisPositionNDArrayFail() {
      scalar.divi(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void diviAxisPositionScalarFail() {
      scalar.divi(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void diviNDArrayFail() {
      scalar.divi(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void diviScalarFail() {
      scalar.divi(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void mulAxisNDArrayFail() {
      scalar.mul(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void mulAxisPositionNDArrayFail() {
      scalar.mul(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void mulAxisPositionScalarFail() {
      scalar.mul(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void mulNDArrayFail() {
      scalar.mul(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void mulScalarFail() {
      scalar.mul(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void muliAxisNDArrayFail() {
      scalar.muli(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void muliAxisPositionNDArrayFail() {
      scalar.muli(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void muliAxisPositionScalarFail() {
      scalar.muli(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void muliNDArrayFail() {
      scalar.muli(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void muliScalarFail() {
      scalar.muli(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdivAxisNDArrayFail() {
      scalar.rdiv(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdivAxisPositionNDArrayFail() {
      scalar.rdiv(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdivAxisPositionScalarFail() {
      scalar.rdiv(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdivNDArrayFail() {
      scalar.rdiv(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void rdivScalarFail() {
      scalar.rdiv(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdiviAxisNDArrayFail() {
      scalar.rdivi(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdiviAxisPositionNDArrayFail() {
      scalar.rdivi(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdiviAxisPositionScalarFail() {
      scalar.rdivi(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdiviNDArrayFail() {
      scalar.rdivi(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rdiviScalarFail() {
      scalar.rdivi(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubAxisNDArrayFail() {
      scalar.rsub(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubAxisPositionNDArrayFail() {
      scalar.rsub(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubAxisPositionScalarFail() {
      scalar.rsub(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubNDArrayFail() {
      scalar.rsub(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void rsubScalarFail() {
      scalar.rsub(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubiAxisNDArrayFail() {
      scalar.rsubi(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubiAxisPositionNDArrayFail() {
      scalar.rsubi(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubiAxisPositionScalarFail() {
      scalar.rsubi(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubiNDArrayFail() {
      scalar.rsubi(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void rsubiScalarFail() {
      scalar.rsubi(1);
   }

   @Test
   public void set() {
      assertEquals(factory.array(new double[]{1, 2, 3, 10}),
                   factory.arange(1, 5).set(3, "10.0"));
      assertEquals(factory.array(new double[]{1, 2, 3, 10}),
                   factory.arange(1, 5).set(0, 3, "10.0"));
      assertEquals(factory.array(new double[]{1, 2, 3, 10}),
                   factory.arange(1, 5).set(0, 0, 3, "10.0"));
      assertEquals(factory.array(new double[]{1, 2, 3, 10}),
                   factory.arange(1, 5).set(0, 0, 0, 3, "10.0"));
      assertEquals(factory.array(new String[][]{
            {"A", "Z", "C", "D"},
            {"E", "Z", "G", "H"},
            {"I", "Z", "K", "L"}
      }), matrix34.copy().setAxis(Shape.COLUMN, 1, "Z"));
      assertEquals(factory.array(new String[][]{
            {"A", "Z", "C", "D"},
            {"E", "Z", "G", "H"},
            {"I", "Z", "K", "L"}
      }), matrix34.copy().setAxis(Shape.COLUMN, 1, factory.array(new String[]{"Z", "Z", "Z"})));

      assertEquals(factory.array(new String[][]{
                         {"A", "Z", "C", "D"},
                         {"E", "Z", "G", "H"},
                         {"I", "Z", "K", "L"}
                   }), matrix34.copy()
                               .set(0, 1, "Z")
                               .set(1, 1, "Z")
                               .set(2, 1, "Z")
      );

      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), "Z"));
      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), factory.zeros(3, 4)
                                                                                                            .fill("Z")));
      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), factory.zeros(4).fill("Z")));

      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setSlice(0, factory.zeros(3, 4).fill("Z")));

   }

   @Test(expected = IllegalArgumentException.class)
   public void subAxisNDArrayFail() {
      scalar.sub(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subAxisPositionNDArrayFail() {
      scalar.sub(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subAxisPositionScalarFail() {
      scalar.sub(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subNDArrayFail() {
      scalar.sub(scalar);
   }

   @Test(expected = IllegalStateException.class)
   public void subScalarFail() {
      scalar.sub(1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subiAxisNDArrayFail() {
      scalar.subi(Shape.ROW, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subiAxisPositionNDArrayFail() {
      scalar.subi(Shape.ROW, 0, scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subiAxisPositionScalarFail() {
      scalar.subi(Shape.ROW, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subiNDArrayFail() {
      scalar.subi(scalar);
   }

   @Test(expected = IllegalArgumentException.class)
   public void subiScalarFail() {
      scalar.subi(1);
   }

}
