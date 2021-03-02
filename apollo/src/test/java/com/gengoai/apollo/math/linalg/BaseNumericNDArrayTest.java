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

import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.conversion.Cast;
import com.gengoai.json.Json;
import com.gengoai.math.Operator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.gengoai.apollo.math.linalg.Shape.*;
import static com.gengoai.apollo.math.linalg.nd.DINT32;
import static org.junit.Assert.*;

public abstract class BaseNumericNDArrayTest {
   protected final NumericNDArray scalar;
   protected final NumericNDArray rowVector;
   protected final NumericNDArray colVector;
   protected final NumericNDArray matrix34;
   protected final NumericNDArray matrix43;
   protected final NumericNDArray tensor243;
   protected final NumericNDArray tensor234;
   protected final NumericNDArray tensor2234;
   protected final NumericNDArrayFactory factory;

   public BaseNumericNDArrayTest(NumericNDArrayFactory factory) {
      this.scalar = factory.zeros(1).fill(1);
      this.colVector = factory.array(new float[][]{{1}, {1}, {1}, {1}});
      this.rowVector = factory.array(new float[]{1, 1, 1, 1});
      this.matrix34 = factory.arange(shape(3, 4), 1);
      this.matrix43 = factory.arange(shape(4, 3), 1);
      this.tensor243 = factory.arange(shape(2, 4, 3), 1);
      this.tensor234 = factory.arange(shape(2, 3, 4), 1);
      this.tensor2234 = factory.arange(shape(2, 2, 3, 4), 1);
      this.factory = factory;
   }

   @Test
   public void T() {
      assertEquals(factory.empty(), factory.empty().T());
      assertEquals(factory.ones(1, 4), colVector.T());
      assertEquals(factory.ones(4, 1), rowVector.T());

      assertEquals(factory.empty(), factory.empty().transpose(ROW, COLUMN));

      assertEquals(factory.array(new int[][]{
            {1, 5, 9},
            {2, 6, 10},
            {3, 7, 11},
            {4, 8, 12}
      }), matrix34.T());

      assertEquals(factory.array(new int[][][]{
            {
                  {1, 4, 7, 10},
                  {2, 5, 8, 11},
                  {3, 6, 9, 12}
            },
            {
                  {13, 16, 19, 22},
                  {14, 17, 20, 23},
                  {15, 18, 21, 24}
            }
      }), tensor243.T());

      assertEquals(factory.array(new int[][][]{
            {
                  {1, 13},
                  {4, 16},
                  {7, 19},
                  {10, 22}
            },
            {
                  {2, 14},
                  {5, 17},
                  {8, 20},
                  {11, 23}

            },
            {
                  {3, 15},
                  {6, 18},
                  {9, 21},
                  {12, 24}

            }
      }), tensor243.transpose(COLUMN, ROW, CHANNEL));

      assertEquals(factory.array(new int[][][][]{
            {{{1, 5, 9},
                  {2, 6, 10},
                  {3, 7, 11},
                  {4, 8, 12}},

                  {{13, 17, 21},
                        {14, 18, 22},
                        {15, 19, 23},
                        {16, 20, 24}}},


            {{{25, 29, 33},
                  {26, 30, 34},
                  {27, 31, 35},
                  {28, 32, 36}},

                  {{37, 41, 45},
                        {38, 42, 46},
                        {39, 43, 47},
                        {40, 44, 48}

                  }}}), tensor2234.T());

      assertEquals(factory.array(new int[][][][]{
            {{{1, 2, 3, 4},
                  {5, 6, 7, 8},
                  {9, 10, 11, 12}},

                  {{25, 26, 27, 28},
                        {29, 30, 31, 32},
                        {33, 34, 35, 36}}},


            {{{13, 14, 15, 16},
                  {17, 18, 19, 20},
                  {21, 22, 23, 24}},

                  {{37, 38, 39, 40},
                        {41, 42, 43, 44},
                        {45, 46, 47, 48}}}
      }), tensor2234.transpose(CHANNEL, KERNEL, ROW, COLUMN));


   }

   @Test
   public void addColVector() {
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.add(colVector));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.add(COLUMN, colVector));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.add(COLUMN, colVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{2, 6, 10})),
                   matrix34.add(COLUMN, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{2, 6, 10})),
                   matrix34.add(COLUMN, 0, factory.ones(3, 1)));

      assertEquals(factory.arange(shape(4, 3), 2), matrix43.copy().addi(colVector));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.copy().addi(COLUMN, colVector));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.copy().addi(COLUMN, colVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{2, 6, 10})),
                   matrix34.copy().addi(COLUMN, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{2, 6, 10})),
                   matrix34.copy().addi(COLUMN, 0, factory.ones(3, 1)));
   }

   @Test
   public void addNDArray() {
      assertEquals(factory.empty(), factory.empty().add(factory.empty()));
      assertEquals(factory.zeros(4, 1).fill(2d), colVector.add(scalar));
      assertEquals(factory.zeros(4).fill(2d), rowVector.add(scalar));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.add(scalar));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.add(scalar));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.add(scalar));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.add(scalar));
      assertEquals(factory.arange(shape(2, 2, 3, 4), 2), tensor2234.add(Shape.KERNEL, factory.ones(1, 3, 4)));
      assertEquals(factory.arange(shape(2, 2, 3, 4), 2), tensor2234.add(factory.ones(1)));

      assertEquals(factory.zeros(4, 1).fill(2d), colVector.copy().addi(scalar));
      assertEquals(factory.zeros(4).fill(2d), rowVector.copy().addi(scalar));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.copy().addi(scalar));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.copy().addi(scalar));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.copy().addi(scalar));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.copy().addi(scalar));
      assertEquals(factory.arange(shape(2, 2, 3, 4), 2), tensor2234.copy().addi(Shape.KERNEL, factory.ones(1, 3, 4)));
      assertEquals(factory.arange(shape(2, 2, 3, 4), 2), tensor2234.copy().addi(factory.ones(1)));
   }

   @Test
   public void addRowVector() {
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.add(rowVector));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.add(ROW, rowVector));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.add(ROW, rowVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(2, 6)),
                   matrix34.add(ROW, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(2, 6)),
                   matrix34.add(ROW, 0, factory.ones(4)));

      assertEquals(factory.arange(shape(3, 4), 2), matrix34.copy().addi(rowVector));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.copy().addi(ROW, rowVector));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.copy().addi(ROW, rowVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(2, 6)),
                   matrix34.copy().addi(ROW, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(2, 6)),
                   matrix34.copy().addi(ROW, 0, factory.ones(4)));


   }

   @Test
   public void addScalar() {
      assertEquals(factory.zeros(4, 1).fill(2d), colVector.add(1));
      assertEquals(factory.zeros(4).fill(2d), rowVector.add(1));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.add(1));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.add(1));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.add(1));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.add(1));


      assertEquals(factory.zeros(4, 1).fill(2d), colVector.copy().addi(1));
      assertEquals(factory.zeros(4).fill(2d), rowVector.copy().addi(1));
      assertEquals(factory.arange(shape(3, 4), 2), matrix34.copy().addi(1));
      assertEquals(factory.arange(shape(4, 3), 2), matrix43.copy().addi(1));
      assertEquals(factory.arange(shape(2, 4, 3), 2), tensor243.copy().addi(1));
      assertEquals(factory.arange(shape(2, 3, 4), 2), tensor234.copy().addi(1));
   }

   @Test
   public void argMax() {
      assertNull(factory.empty().argMax());
      assertEquals(Index.zero(), factory.zeros(1).fill(10d).argMax());
      assertEquals(Index.zero(), colVector.argMax());
      assertEquals(Index.zero(), rowVector.argMax());
      assertEquals(Index.index(3, 2), matrix43.argMax());
      assertEquals(Index.index(2, 3), matrix34.argMax());
      assertEquals(Index.index(1, 2, 3), tensor234.argMax());
      assertEquals(Index.index(1, 3, 2), tensor243.argMax());


      assertTrue(factory.empty().argMax(COLUMN).isEmpty());
      assertEquals(DINT32.zeros(1), colVector.argMax(ROW));


      assertEquals(DINT32.zeros(1), colVector.argMax(ROW));
      assertEquals(DINT32.zeros(1), rowVector.argMax(COLUMN));

      assertEquals(DINT32.array(new int[]{2, 2, 2, 2}), matrix34.argMax(ROW));
      assertEquals(DINT32.array(new int[]{3, 3, 3}), matrix34.argMax(COLUMN));
      assertEquals(DINT32.array(new int[][][]{
            {
                  {3, 3, 3},
                  {3, 3, 3}
            },
            {
                  {3, 3, 3},
                  {3, 3, 3}
            }
      }), tensor2234.argMax(COLUMN));
      assertEquals(DINT32.array(new int[][][]{
            {
                  {2, 2, 2, 2},
                  {2, 2, 2, 2}
            },
            {
                  {2, 2, 2, 2},
                  {2, 2, 2, 2}
            }
      }), tensor2234.argMax(ROW));
      assertEquals(DINT32.array(new int[][][]{
            {
                  {1, 1, 1, 1},
                  {1, 1, 1, 1},
                  {1, 1, 1, 1}
            },
            {
                  {1, 1, 1, 1},
                  {1, 1, 1, 1},
                  {1, 1, 1, 1}
            }
      }), tensor2234.argMax(KERNEL));
      assertEquals(DINT32.array(new int[][][]{
            {
                  {1, 1, 1, 1},
                  {1, 1, 1, 1},
                  {1, 1, 1, 1}
            },
            {
                  {1, 1, 1, 1},
                  {1, 1, 1, 1},
                  {1, 1, 1, 1}
            }
      }), tensor2234.argMax(CHANNEL));

   }

   @Test
   public void argMin() {
      assertNull(factory.empty().argMin());
      assertEquals(Index.zero(), factory.zeros(1).fill(10d).argMin());
      assertEquals(Index.zero(), colVector.argMin());
      assertEquals(Index.zero(), rowVector.argMin());
      assertEquals(Index.zero(), matrix43.argMin());
      assertEquals(Index.zero(), matrix34.argMin());
      assertEquals(Index.zero(), tensor234.argMin());
      assertEquals(Index.zero(), tensor243.argMin());


      assertEquals(DINT32.zeros(4, 3), tensor243.argMin(CHANNEL));
      assertEquals(DINT32.zeros(2, 3), tensor243.argMin(ROW));
      assertEquals(DINT32.zeros(2, 4), tensor243.argMin(COLUMN));
      assertEquals(DINT32.zeros(2, 3, 4), tensor2234.argMin(KERNEL));
   }

   @Test
   public void asType() {
//      assertEquals(colVector, colVector.asType(factory.getType()));
//      Class<?> wrapped = Primitives.wrap(factory.getType());
//      if (wrapped == Float.class) {
//         assertEquals(nd.DSTRING.array(new float[]{1f, 1f, 1f, 1f}),
//                      rowVector.asType(String.class));
//      } else {
//         assertEquals(nd.DSTRING.array(new int[]{1, 1, 1, 1}),
//                      rowVector.asType(String.class));
//      }
   }

   @Test
   public void decode() {
      Encoder e = new FixedEncoder(List.of("A", "B", "C"));
      NumericNDArray n = factory.array(new double[][]{
            {10, 0, 0},
            {2, 6, 3},
            {4, 1, 5}
      });
      Sequence<?> o = n.decodeSequence(e, SequenceValidator.ALWAYS_TRUE);
      assertEquals(List.of("A", "B", "C"), List.of(o.get(0).asVariable().getName(),
                                                   o.get(1).asVariable().getName(),
                                                   o.get(2).asVariable().getName()));
      o = n.decodeSequence(e, new SequenceValidator() {
         @Override
         public boolean isValid(String currentLabel, String previousLabel, Observation instance) {
            return !(currentLabel.equals("B") && previousLabel.equals("A"));
         }
      });
      assertEquals(List.of("A", "C", "C"), List.of(o.get(0).asVariable().getName(),
                                                   o.get(1).asVariable().getName(),
                                                   o.get(2).asVariable().getName()));

   }

   @Test
   public void divColVector() {
      assertEquals(matrix43, matrix43.div(colVector));
      assertEquals(matrix43, matrix43.div(COLUMN, colVector));
      assertEquals(tensor243, tensor243.div(COLUMN, colVector));
      assertEquals(matrix34, matrix34.div(COLUMN, 0, 1));
      assertEquals(matrix34, matrix34.div(COLUMN, 0, factory.ones(3, 1)));

      assertEquals(matrix43, matrix43.copy().divi(colVector));
      assertEquals(matrix43, matrix43.copy().divi(COLUMN, colVector));
      assertEquals(tensor243, tensor243.copy().divi(COLUMN, colVector));
      assertEquals(matrix34, matrix34.copy().divi(COLUMN, 0, 1));
      assertEquals(matrix34, matrix34.copy().divi(COLUMN, 0, factory.ones(3, 1)));
   }

   @Test
   public void divNDArray() {
      assertEquals(factory.empty(), factory.empty().div(factory.empty()));
      assertEquals(colVector, colVector.div(scalar));
      assertEquals(rowVector, rowVector.div(scalar));
      assertEquals(matrix34, matrix34.div(scalar));
      assertEquals(matrix43, matrix43.div(scalar));
      assertEquals(tensor243, tensor243.div(scalar));
      assertEquals(tensor234, tensor234.div(scalar));

      assertEquals(factory.empty(), factory.empty().divi(factory.empty()));
      assertEquals(colVector, colVector.copy().divi(scalar));
      assertEquals(rowVector, rowVector.copy().divi(scalar));
      assertEquals(matrix34, matrix34.copy().divi(scalar));
      assertEquals(matrix43, matrix43.copy().divi(scalar));
      assertEquals(tensor243, tensor243.copy().divi(scalar));
      assertEquals(tensor234, tensor234.copy().divi(scalar));
   }

   @Test
   public void divRowVector() {
      assertEquals(matrix34, matrix34.div(rowVector));
      assertEquals(matrix34, matrix34.div(ROW, rowVector));
      assertEquals(tensor234, tensor234.div(ROW, rowVector));
      assertEquals(matrix34, matrix34.div(ROW, 0, 1));
      assertEquals(matrix34, matrix34.div(ROW, 0, factory.ones(4)));

      assertEquals(matrix34, matrix34.copy().divi(rowVector));
      assertEquals(matrix34, matrix34.copy().divi(ROW, rowVector));
      assertEquals(tensor234, tensor234.copy().divi(ROW, rowVector));
      assertEquals(matrix34, matrix34.copy().divi(ROW, 0, 1));
      assertEquals(matrix34, matrix34.copy().divi(ROW, 0, factory.ones(4)));
   }

   @Test
   public void divScalar() {
      assertEquals(factory.ones(4, 1), colVector.div(1));
      assertEquals(factory.ones(4), rowVector.div(1));
      assertEquals(matrix34, matrix34.div(1));
      assertEquals(matrix43, matrix43.div(1));
      assertEquals(tensor243, tensor243.div(1));
      assertEquals(tensor234, tensor234.div(1));

      assertEquals(factory.ones(4, 1), colVector.copy().divi(1));
      assertEquals(factory.ones(4), rowVector.copy().divi(1));
      assertEquals(matrix34, matrix34.copy().divi(1));
      assertEquals(matrix43, matrix43.copy().divi(1));
      assertEquals(tensor243, tensor243.copy().divi(1));
      assertEquals(tensor234, tensor234.copy().divi(1));
   }

   @Test
   public void dot() {
      assertEquals(0, factory.empty().dot(factory.empty()), 0d);
      assertEquals(4, colVector.dot(rowVector), 0d);
      assertEquals(factory.array(new double[]{
            15.000000,
            18.000000,
            21.000000,
            24.000000
      }), matrix34.dot(rowVector, ROW));
      assertEquals(factory.array(new double[]{
            6.000000,
            15.000000,
            24.000000,
            33.000000
      }), matrix43.dot(colVector, COLUMN));
   }

   @Test
   public void fill() {
      assertNotEquals(matrix43, matrix43.copy().fill(23));
      assertEquals(factory.ones(4, 3), matrix43.copy().fill(1));

      assertEquals(factory.ones(4, 3), matrix43.copy().fillIf(d -> d > 0, 1));
   }

   @Test
   public void forEachSparse() {
      NumericNDArray s = factory.array(new double[]{0.0, 0.0, 2});
      List<Number> out = new ArrayList<>();
      s.forEachSparse((i, v) -> out.add(v));
      assertArrayEquals(new Number[]{(Number) factory.scalar(2).scalar()},
                        out.toArray(new Number[0]));
   }

   @Test
   public void get() {

      assertEquals(1, tensor2234.get(0).intValue());
      assertEquals(1, tensor2234.get(0, 0).intValue());
      assertEquals(1, tensor2234.get(0, 0, 0).intValue());
      assertEquals(1, tensor2234.get(0, 0, 0, 0).intValue());
      assertEquals(1, tensor2234.get(Index.zero()).intValue());

      assertEquals(2, tensor2234.get(3).intValue());
      assertEquals(5, tensor2234.get(1, 0).intValue());
      assertEquals(13, tensor2234.get(1, 0, 0).intValue());
      assertEquals(25, tensor2234.get(1, 0, 0, 0).intValue());
      assertEquals(48, tensor2234.get(Index.index(1, 1, 2, 3)).intValue());

      assertEquals(1, colVector.get(3).intValue());
      assertEquals(1, rowVector.get(3).intValue());
      assertEquals(1, colVector.get(1, 0).intValue());
      assertEquals(1, rowVector.get(0, 3).intValue());


      assertEquals(factory.arange(Shape.shape(2, 3, 4), 1),
                   tensor2234.getAxis(KERNEL, 0));


      assertEquals(factory.array(new int[][][]{
                         {
                               {1, 2, 3, 4},
                               {13, 14, 15, 16},
                         },
                         {
                               {25, 26, 27, 28},
                               {37, 38, 39, 40},
                         }

                   }),
                   tensor2234.getAxis(ROW, 0));


      assertEquals(factory.array(new int[]{1, 2}),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(2))));

      assertEquals(factory.array(new int[][]{
                         {1, 2},
                         {5, 6}
                   }),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(2, 2))));

      assertEquals(factory.array(new int[][][]{
                         {{1, 2},
                               {5, 6}},
                         {{13, 14},
                               {17, 18}}
                   }),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(2, 2, 2))));

      assertEquals(factory.array(new int[][]{
                         {1, 2, 3, 4},
                         {13, 14, 15, 16}
                   }),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(1,
                                                                      tensor2234.shape().channels(),
                                                                      1,
                                                                      tensor234.shape().columns()))));

      assertEquals(factory.array(new int[]{
                         1, 2, 3, 4
                   }),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(1,
                                                                      1,
                                                                      1,
                                                                      tensor234.shape().columns()))));
      assertEquals(factory.array(new int[]{
                         1
                   }),
                   tensor2234.get(Index.zero().iteratorTo(Index.index(1,
                                                                      1,
                                                                      1,
                                                                      1))));

      assertEquals(1, tensor2234.getDouble(0), 0d);
      assertEquals(1, tensor2234.getDouble(0, 0), 0d);
      assertEquals(1, tensor2234.getDouble(0, 0, 0), 0d);
      assertEquals(1, tensor2234.getDouble(0, 0, 0, 0), 0d);
      assertEquals(1, tensor2234.getDouble(Index.zero()), 0d);

      assertEquals(2, tensor2234.getDouble(3), 0d);
      assertEquals(5, tensor2234.getDouble(1, 0), 0d);
      assertEquals(13, tensor2234.getDouble(1, 0, 0), 0d);
      assertEquals(25, tensor2234.getDouble(1, 0, 0, 0), 0d);
      assertEquals(48, tensor2234.getDouble(Index.index(1, 1, 2, 3)), 0d);

      assertEquals(1, colVector.getDouble(3), 0d);
      assertEquals(1, rowVector.getDouble(3), 0d);
      assertEquals(1, colVector.getDouble(1, 0), 0d);
      assertEquals(1, rowVector.getDouble(0, 3), 0d);

   }

   @Test
   public void json() throws Exception {
      String json = Json.dumps(tensor2234);
      assertEquals(tensor2234, Json.parse(json, NDArray.class));
   }

   @Test
   public void label() {
      NumericNDArray n = factory.zeros(3, 4);
      assertNull(n.getLabel());
      n.setLabel("TESTING");
      assertEquals("TESTING", n.getLabel());
      n.setLabel(23d);
      assertEquals(23d, n.getLabel(), 0d);
   }

   @Test
   public void map() {
      assertEquals(factory.arange(Shape.shape(3, 4), 2),
                   matrix34.mapDouble(i -> i + 1));
      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 2),
                   tensor2234.mapDouble(i -> i + 1));
      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 2),
                   tensor2234.mapDouble(1, Operator::add));

      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 2, 2)
                          .setSlice(1, 0, Cast.as(factory.arange(Shape.shape(3, 4), 26, 2)))
                          .setSlice(1, 1, Cast.as(factory.arange(Shape.shape(3, 4), 50, 2))),
                   tensor2234.mapDouble(tensor234, Operator::add));

      assertEquals(factory.array(new int[]{2, 2, 2, 2}), rowVector.mapDouble(colVector, Operator::add));


   }

   @Test
   public void max() {
      assertNull(factory.empty().max());
      assertEquals(10d, factory.zeros(1).fill(10d).max().doubleValue(), 0d);
      assertEquals(1, colVector.max().doubleValue(), 0d);
      assertEquals(1, rowVector.max().doubleValue(), 0d);
      assertEquals(12, matrix43.max().doubleValue(), 0d);
      assertEquals(12, matrix34.max().doubleValue(), 0d);
      assertEquals(24, tensor234.max().doubleValue(), 0d);
      assertEquals(24, tensor243.max().doubleValue(), 0d);


      assertEquals(factory.array(new int[][][]{
            {
                  {4, 8, 12},
                  {16, 20, 24}
            },
            {
                  {28, 32, 36},
                  {40, 44, 48}
            }
      }), tensor2234.max(COLUMN));

      assertEquals(factory.array(new int[][][]{
            {
                  {9, 10, 11, 12},
                  {21, 22, 23, 24}
            },
            {
                  {33, 34, 35, 36},
                  {45, 46, 47, 48}
            }
      }), tensor2234.max(ROW));

      assertEquals(factory.array(new int[][]{
            {37, 38, 39, 40},
            {41, 42, 43, 44},
            {45, 46, 47, 48}
      }), tensor2234.max(KERNEL, CHANNEL));


      assertEquals(factory.array(new int[]{45, 46, 47, 48}), tensor2234.max(KERNEL, CHANNEL, ROW));
      assertEquals(factory.array(new int[]{40, 44, 48}), tensor2234.max(KERNEL, CHANNEL, COLUMN));
      assertEquals(factory.array(new int[]{48}), tensor2234.max(KERNEL, CHANNEL, ROW, COLUMN));
   }

   @Test
   public void mean() {
      assertEquals(Double.NaN, factory.empty().mean(), 0);
      assertEquals(1, colVector.mean(), 0d);
      assertEquals(nd.DFLOAT32.array(new float[]{5.000000f, 6.000000f, 7.000000f, 8.000000f}),
                   matrix34.mean(ROW));
   }

   @Test
   public void min() {
      assertNull(factory.empty().min());
      assertEquals(10d, factory.zeros(1).fill(10d).min().doubleValue(), 0d);
      assertEquals(1d, colVector.min().doubleValue(), 0d);
      assertEquals(1d, rowVector.min().doubleValue(), 0d);
      assertEquals(1d, matrix43.min().doubleValue(), 0d);
      assertEquals(1d, matrix34.min().doubleValue(), 0d);
      assertEquals(1d, tensor234.min().doubleValue(), 0d);
      assertEquals(1d, tensor243.min().doubleValue(), 0d);

      assertEquals(factory.ones(1), rowVector.min(COLUMN));
   }

   @Test
   public void mulColVector() {
      assertEquals(matrix43, matrix43.mul(colVector));
      assertEquals(matrix43, matrix43.mul(COLUMN, colVector));
      assertEquals(tensor243, tensor243.mul(COLUMN, colVector));
      assertEquals(matrix34, matrix34.mul(COLUMN, 0, 1));
      assertEquals(matrix34, matrix34.mul(COLUMN, 0, factory.ones(3, 1)));

      assertEquals(matrix43, matrix43.copy().muli(colVector));
      assertEquals(matrix43, matrix43.copy().muli(COLUMN, colVector));
      assertEquals(tensor243, tensor243.copy().muli(COLUMN, colVector));
      assertEquals(matrix34, matrix34.copy().muli(COLUMN, 0, 1));
      assertEquals(matrix34, matrix34.copy().muli(COLUMN, 0, factory.ones(3, 1)));
   }

   @Test
   public void mulNDArray() {
      assertEquals(factory.empty(), factory.empty().mul(factory.empty()));
      assertEquals(colVector, colVector.mul(scalar));
      assertEquals(rowVector, rowVector.mul(scalar));
      assertEquals(matrix34, matrix34.mul(scalar));
      assertEquals(matrix43, matrix43.mul(scalar));
      assertEquals(tensor243, tensor243.mul(scalar));
      assertEquals(tensor234, tensor234.mul(scalar));

      assertEquals(factory.empty(), factory.empty().muli(factory.empty()));
      assertEquals(colVector, colVector.copy().muli(scalar));
      assertEquals(rowVector, rowVector.copy().muli(scalar));
      assertEquals(matrix34, matrix34.copy().muli(scalar));
      assertEquals(matrix43, matrix43.copy().muli(scalar));
      assertEquals(tensor243, tensor243.copy().muli(scalar));
      assertEquals(tensor234, tensor234.copy().muli(scalar));
   }

   @Test
   public void mulRowVector() {
      assertEquals(matrix34, matrix34.mul(rowVector));
      assertEquals(matrix34, matrix34.mul(ROW, rowVector));
      assertEquals(tensor234, tensor234.mul(ROW, rowVector));
      assertEquals(matrix34, matrix34.mul(ROW, 0, 1));
      assertEquals(matrix34, matrix34.mul(ROW, 0, factory.ones(4)));

      assertEquals(matrix34, matrix34.copy().muli(rowVector));
      assertEquals(matrix34, matrix34.copy().muli(ROW, rowVector));
      assertEquals(tensor234, tensor234.copy().muli(ROW, rowVector));
      assertEquals(matrix34, matrix34.copy().muli(ROW, 0, 1));
      assertEquals(matrix34, matrix34.copy().muli(ROW, 0, factory.ones(4)));
   }

   @Test
   public void mulScalar() {
      assertEquals(factory.ones(4, 1), colVector.mul(1));
      assertEquals(factory.ones(4), rowVector.mul(1));
      assertEquals(matrix34, matrix34.mul(1));
      assertEquals(matrix43, matrix43.mul(1));
      assertEquals(tensor243, tensor243.mul(1));
      assertEquals(tensor234, tensor234.mul(1));

      assertEquals(factory.ones(4, 1), colVector.copy().muli(1));
      assertEquals(factory.ones(4), rowVector.copy().muli(1));
      assertEquals(matrix34, matrix34.copy().muli(1));
      assertEquals(matrix43, matrix43.copy().muli(1));
      assertEquals(tensor243, tensor243.copy().muli(1));
      assertEquals(tensor234, tensor234.copy().muli(1));
   }

   @Test
   public void norm() {
      assertEquals(factory.array(new double[][]{
            {46.518814, 48.166378, 49.839745, 51.536396},
            {53.254108, 54.990910, 56.745045, 58.514954},
            {60.299255, 62.096699, 63.906181, 65.726707}
      }), tensor2234.norm2(KERNEL, CHANNEL));

      assertEquals(factory.array(new double[][]{
            {51.594574, 53.591045, 55.623737, 57.688820},
            {77.291656, 79.548729, 81.816872, 84.095184}
      }), tensor2234.norm2(KERNEL, ROW));

      assertEquals(factory.array(new double[][]{
            {53.329166, 62.449982, 72.194183},
            {82.340752, 92.757751, 103.363434}
      }), tensor2234.norm2(KERNEL, COLUMN));

      assertEquals(factory.array(new double[][]{
            {31.717503, 33.823071, 35.972210, 38.157570},
            {87.349869, 89.755226, 92.162903, 94.572723}
      }), tensor2234.norm2(ROW, CHANNEL));

      assertEquals(factory.array(new double[][]{
            {29.597298, 39.344631, 49.759422},
            {93.530746, 104.670914, 115.844727}
      }), tensor2234.norm2(COLUMN, CHANNEL));

      assertEquals(factory.array(new double[][]{
            {25.495098, 65.192024},
            {106.329674, 147.709167}
      }), tensor2234.norm2(COLUMN, ROW));


      assertEquals(factory.array(new double[][]{
            {50., 52., 54., 56.},
            {58., 60., 62., 64.},
            {66., 68., 70., 72.}
      }), tensor2234.norm1(KERNEL, CHANNEL));

      assertEquals(factory.array(new double[][]{
            {42., 44., 46., 48.},
            {66., 68., 70., 72.}
      }), tensor2234.norm1(KERNEL, ROW));

      assertEquals(factory.array(new double[][]{
            {32., 40., 48.},
            {56., 64., 72.}
      }), tensor2234.norm1(KERNEL, COLUMN));

      assertEquals(factory.array(new double[][]{
            {30., 32., 34., 36.},
            {78., 80., 82., 84.}
      }), tensor2234.norm1(CHANNEL, ROW));

      assertEquals(factory.array(new double[][]{
            {20., 28., 36.},
            {68., 76., 84.}
      }), tensor2234.norm1(COLUMN, CHANNEL));

      assertEquals(factory.array(new double[][]{
            {24., 60.},
            {96., 132.}
      }), tensor2234.norm1(COLUMN, ROW));


      assertEquals(factory.array(new int[]{10, 26, 42}), matrix34.norm1(COLUMN));
      assertEquals(factory.array(new double[]{15., 18., 21., 24.}), matrix34.norm1(ROW));
      assertEquals(factory.array(new double[]{24.}), matrix34.norm1(ROW, COLUMN));

      assertEquals(6, factory.arange(1, 4).norm1(), 0d);
      assertEquals(factory.array(new int[]{6}), factory.arange(1, 4).norm1(COLUMN));

   }

   @Test
   public void pad() {
      NumericNDArray n = factory.zeros(2, 2, 3, 4);
      n.slice(0).setAxisDouble(ROW, 0, rowVector);
      assertEquals(n, rowVector.padPost(Shape.shape(2, 2, 3, 4)));

      n = factory.zeros(2, 2, 3, 1);
      n.set(0, 0, 0, 0, 1);
      assertEquals(n, rowVector.padPost(Shape.shape(2, 2, 3, 1)));

      n = factory.zeros(10, 4);
      n.setRangeDouble(Index.zero().iteratorTo(matrix34.shape()), matrix34);
      assertEquals(n, matrix34.padPost(ROW, 10));

      n = factory.zeros(10, 11);
      n.setRangeDouble(Index.zero().iteratorTo(matrix34.shape()), matrix34);
      assertEquals(n, matrix34.padPost(ROW, 10, COLUMN, 11));
   }

   @Test
   public void predicted() {
      NumericNDArray n = factory.zeros(3, 4);
      assertNull(n.getPredicted());
      NumericNDArray p = factory.create(2, 2, 2, NDArrayInitializer.random());
      n.setPredicted(p);
      assertEquals(p, n.getPredicted());
      n.setPredicted(23d);
      assertEquals(23d, n.getPredicted(), 0d);
   }

   @Test
   public void rdiv() {
      assertEquals(factory.empty(), factory.empty().rdiv(1));
      assertEquals(factory.empty(), factory.empty().rdiv(factory.empty()));
      assertEquals(factory.ones(1), factory.ones(1).rdiv(1));
      assertEquals(factory.ones(4), rowVector.rdiv(1d));
      assertEquals(factory.ones(4), rowVector.rdiv(colVector));
      assertEquals(factory.ones(4, 1), colVector.rdiv(1d));
      assertEquals(factory.ones(4, 1), colVector.rdiv(rowVector));


      assertEquals(factory.array(new double[][]{
            {1.0, 1.0 / 2, 1.0 / 3, 1.0 / 4},
            {1.0 / 5, 1.0 / 6, 1.0 / 7, 1.0 / 8},
            {1.0 / 9, 1.0 / 10, 1.0 / 11, 1.0 / 12},
      }), matrix34.rdiv(rowVector));

      assertEquals(factory.array(new double[][]{
            {1.0, 1.0 / 2, 1.0 / 3, 1.0 / 4},
            {1.0 / 5, 1.0 / 6, 1.0 / 7, 1.0 / 8},
            {1.0 / 9, 1.0 / 10, 1.0 / 11, 1.0 / 12},
      }), matrix34.rdiv(COLUMN, factory.ones(3, 1)));


      assertEquals(factory.arange(Shape.shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.array(new double[]{1.0, 1.0 / 2, 1.0 / 3, 1.0 / 4})),
                   matrix34.rdiv(ROW, 0, factory.ones(4)));
   }

   @Test
   public void rsub() {
      assertEquals(factory.empty(), factory.empty().rsub(1));
      assertEquals(factory.empty(), factory.empty().rsub(factory.empty()));
      assertEquals(factory.zeros(1), factory.zeros(1).fill(1d).rsub(1));
      assertEquals(factory.zeros(4), rowVector.rsub(1d));
      assertEquals(factory.zeros(4), rowVector.rsub(colVector));
      assertEquals(factory.zeros(4, 1), colVector.rsub(1d));
      assertEquals(factory.zeros(4, 1), colVector.rsub(rowVector));
      assertEquals(factory.array(new int[][]{
            {0, -1, -2, -3},
            {-4, -5, -6, -7},
            {-8, -9, -10, -11}
      }), matrix34.rsub(1));
      assertEquals(factory.array(new int[][]{
            {0, -1, -2, -3},
            {-4, -5, -6, -7},
            {-8, -9, -10, -11}
      }), matrix34.rsub(factory.ones(3, 4)));

      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.array(new int[][][][]{
                                {
                                      {
                                            {0, -1, -2, -3}
                                      },
                                      {
                                            {-12, -13, -14, -15}
                                      }
                                },
                                {
                                      {
                                            {-24, -25, -26, -27}
                                      },
                                      {
                                            {-36, -37, -38, -39}
                                      }
                                }
                          })), tensor2234.rsub(ROW, 0, 1));

      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.array(new int[][][][]{
                                {
                                      {
                                            {0, -1, -2, -3}
                                      },
                                      {
                                            {-12, -13, -14, -15}
                                      }
                                },
                                {
                                      {
                                            {-24, -25, -26, -27}
                                      },
                                      {
                                            {-36, -37, -38, -39}
                                      }
                                }
                          })), tensor2234.rsub(ROW, 0, factory.ones(4)));

      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 0, -1),
                   tensor2234.rsub(ROW, factory.ones(4)));

      assertEquals(factory.empty(), factory.empty().copy().rsubi(1));
      assertEquals(factory.empty(), factory.empty().copy().rsubi(factory.empty()));
      assertEquals(factory.zeros(1), factory.zeros(1).fill(1d).copy().rsubi(1));
      assertEquals(factory.zeros(4), rowVector.copy().rsubi(1d));
      assertEquals(factory.zeros(4), rowVector.copy().rsubi(colVector));
      assertEquals(factory.zeros(4, 1), colVector.copy().rsubi(1d));
      assertEquals(factory.zeros(4, 1), colVector.copy().rsubi(rowVector));
      assertEquals(factory.array(new int[][]{
            {0, -1, -2, -3},
            {-4, -5, -6, -7},
            {-8, -9, -10, -11}
      }), matrix34.copy().rsubi(1));
      assertEquals(factory.array(new int[][]{
            {0, -1, -2, -3},
            {-4, -5, -6, -7},
            {-8, -9, -10, -11}
      }), matrix34.copy().rsubi(factory.ones(3, 4)));

      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.array(new int[][][][]{
                                {
                                      {
                                            {0, -1, -2, -3}
                                      },
                                      {
                                            {-12, -13, -14, -15}
                                      }
                                },
                                {
                                      {
                                            {-24, -25, -26, -27}
                                      },
                                      {
                                            {-36, -37, -38, -39}
                                      }
                                }
                          })), tensor2234.copy().rsubi(ROW, 0, 1));
      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.array(new int[][][][]{
                                {
                                      {
                                            {0, -1, -2, -3}
                                      },
                                      {
                                            {-12, -13, -14, -15}
                                      }
                                },
                                {
                                      {
                                            {-24, -25, -26, -27}
                                      },
                                      {
                                            {-36, -37, -38, -39}
                                      }
                                }
                          })), tensor2234.copy().rsubi(ROW, 0, factory.ones(4)));


      assertEquals(factory.arange(Shape.shape(2, 2, 3, 4), 0, -1),
                   tensor2234.copy().rsubi(ROW, factory.ones(4)));

   }

   @Test
   public void set() {
      assertEquals(factory.ones(4), rowVector.setAxisDouble(ROW, 0, factory.ones(4)));
      assertEquals(factory.array(new int[][][][]{
            {
                  {
                        {1, 1},
                        {1, 1}
                  },
                  {
                        {1, 1},
                        {1, 1}
                  }
            },
            {
                  {
                        {0, 0},
                        {0, 0}
                  },
                  {
                        {0, 0},
                        {0, 0}
                  }
            }
      }), factory.zeros(2, 2, 2, 2).setAxisDouble(KERNEL, 0, 1));

      assertEquals(factory.array(new int[]{1, 0, 0, 0}), factory.zeros(4).set(0, 1d));
      assertEquals(factory.array(new int[]{1, 0, 0, 0}), factory.zeros(4).set(0, 0, 1d));
      assertEquals(factory.array(new int[]{1, 0, 0, 0}), factory.zeros(4).set(0, 0, 0, 1d));
      assertEquals(factory.array(new int[]{1, 0, 0, 0}), factory.zeros(4).set(0, 0, 0, 0, 1d));

      assertEquals(factory.array(new int[][]{{1, 0, 0, 0}, {0, 0, 0, 0}}), factory.zeros(2, 4).set(0, 1d));
      assertEquals(factory.array(new int[][]{{0, 0, 0, 0}, {1, 0, 0, 0}}), factory.zeros(2, 4).set(1, 0, 1d));
      assertEquals(factory.array(new int[][][]{{{0, 0, 0, 0}}, {{0, 1, 0, 0}}}), factory.zeros(2, 1, 4)
                                                                                        .set(1, 0, 1, 1d));
   }

   @Test
   public void slice() {
      NumericNDArray s = tensor2234.copy();
      s.setSlice(1, 0, Cast.as(factory.create(3, 4, NDArrayInitializer.random())));
      assertNotEquals(tensor2234, s);
   }

   @Test
   public void sliceRange() {
      assertEquals(matrix34, tensor234.slice(0, 0, 1, 1));
   }

   @Test
   public void subColVector() {
      assertEquals(factory.arange(shape(4, 3)), matrix43.sub(colVector));
      assertEquals(factory.arange(shape(4, 3)), matrix43.sub(COLUMN, colVector));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.sub(COLUMN, colVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{0, 4, 8})),
                   matrix34.sub(COLUMN, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{0, 4, 8})),
                   matrix34.sub(COLUMN, 0, factory.ones(3, 1)));

      assertEquals(factory.arange(shape(4, 3)), matrix43.copy().subi(colVector));
      assertEquals(factory.arange(shape(4, 3)), matrix43.copy().subi(COLUMN, colVector));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.copy().subi(COLUMN, colVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{0, 4, 8})),
                   matrix34.copy().subi(COLUMN, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(COLUMN, 0, factory.array(new int[]{0, 4, 8})),
                   matrix34.copy().subi(COLUMN, 0, factory.ones(3, 1)));
   }

   @Test
   public void subNDArray() {
      assertEquals(factory.empty(), factory.empty().sub(factory.empty()));
      assertEquals(factory.zeros(4, 1), colVector.sub(scalar));
      assertEquals(factory.zeros(4), rowVector.sub(scalar));
      assertEquals(factory.arange(shape(3, 4)), matrix34.sub(scalar));
      assertEquals(factory.arange(shape(4, 3)), matrix43.sub(scalar));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.sub(scalar));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.sub(scalar));


      assertEquals(factory.zeros(4, 1), colVector.copy().subi(scalar));
      assertEquals(factory.zeros(4), rowVector.copy().subi(scalar));
      assertEquals(factory.arange(shape(3, 4)), matrix34.copy().subi(scalar));
      assertEquals(factory.arange(shape(4, 3)), matrix43.copy().subi(scalar));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.copy().subi(scalar));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.copy().subi(scalar));
   }

   @Test
   public void subRowVector() {
      assertEquals(factory.arange(shape(3, 4)), matrix34.sub(rowVector));
      assertEquals(factory.arange(shape(3, 4)), matrix34.sub(ROW, rowVector));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.sub(ROW, rowVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(0, 5)),
                   matrix34.sub(ROW, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(0, 5)),
                   matrix34.sub(ROW, 0, factory.ones(4)));

      assertEquals(factory.arange(shape(3, 4)), matrix34.copy().subi(rowVector));
      assertEquals(factory.arange(shape(3, 4)), matrix34.copy().subi(ROW, rowVector));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.copy().subi(ROW, rowVector));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(0, 5)),
                   matrix34.copy().subi(ROW, 0, 1));
      assertEquals(factory.arange(shape(3, 4), 1)
                          .setAxisDouble(ROW, 0, factory.arange(0, 5)),
                   matrix34.copy().subi(ROW, 0, factory.ones(4)));
   }

   @Test
   public void subScalar() {
      assertEquals(factory.zeros(4, 1), colVector.sub(1));
      assertEquals(factory.zeros(4), rowVector.sub(1));
      assertEquals(factory.arange(shape(3, 4)), matrix34.sub(1));
      assertEquals(factory.arange(shape(4, 3)), matrix43.sub(1));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.sub(1));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.sub(1));

      assertEquals(factory.zeros(4, 1), colVector.copy().subi(1));
      assertEquals(factory.zeros(4), rowVector.copy().subi(1));
      assertEquals(factory.arange(shape(3, 4)), matrix34.copy().subi(1));
      assertEquals(factory.arange(shape(4, 3)), matrix43.copy().subi(1));
      assertEquals(factory.arange(shape(2, 4, 3)), tensor243.copy().subi(1));
      assertEquals(factory.arange(shape(2, 3, 4)), tensor234.copy().subi(1));
   }

   @Test
   public void sum() {
      assertEquals(0d, factory.empty().sum(), 0d);
      assertEquals(10d, factory.zeros(1).fill(10d).sum(), 0d);
      assertEquals(4d, colVector.sum(), 0d);
      assertEquals(4d, rowVector.sum(), 0d);
      assertEquals(78d, matrix43.sum(), 0d);
      assertEquals(78d, matrix34.sum(), 0d);
      assertEquals(300d, tensor234.sum(), 0d);
      assertEquals(300d, tensor243.sum(), 0d);

      assertEquals(factory.empty(), factory.empty().sum(KERNEL));
      assertEquals(factory.zeros(1).fill(1), scalar.sum(KERNEL));
      assertEquals(factory.zeros(1).fill(4), rowVector.sum(COLUMN));
      assertEquals(factory.zeros(1).fill(4), colVector.sum(ROW));
      assertEquals(factory.ones(4), colVector.sum(COLUMN));

      assertEquals(factory.array(new int[][][]{
            {
                  {15, 18, 21, 24},
                  {51, 54, 57, 60}

            },
            {
                  {87, 90, 93, 96},
                  {123, 126, 129, 132}
            },
      }), tensor2234.sum(ROW));

      assertEquals(factory.array(new int[][][]{
            {
                  {10, 26, 42},
                  {58, 74, 90}

            },
            {
                  {106, 122, 138},
                  {154, 170, 186}
            },
      }), tensor2234.sum(COLUMN));

      assertEquals(factory.array(new int[][][]{
            {{26, 28, 30, 32},
                  {34, 36, 38, 40},
                  {42, 44, 46, 48}},
            {{50, 52, 54, 56},
                  {58, 60, 62, 64},
                  {66, 68, 70, 72}}

      }), tensor2234.sum(KERNEL));

      assertEquals(factory.array(new int[][][]{
            {{14, 16, 18, 20},
                  {22, 24, 26, 28},
                  {30, 32, 34, 36}},
            {{62, 64, 66, 68},
                  {70, 72, 74, 76},
                  {78, 80, 82, 84}}
      }), tensor2234.sum(CHANNEL));

      assertEquals(factory.array(new int[][]{
            {102, 108, 114, 120},
            {174, 180, 186, 192}
      }), tensor2234.sum(KERNEL, ROW));

      assertEquals(factory.array(new int[][]{
            {116, 148, 180},
            {212, 244, 276}
      }), tensor2234.sum(KERNEL, COLUMN));

      assertEquals(factory.array(new int[][]{
            {76, 80, 84, 88},
            {92, 96, 100, 104},
            {108, 112, 116, 120}
      }), tensor2234.sum(KERNEL, CHANNEL));
   }

   @Test
   public void sumOfSquares() {
      assertEquals(factory.empty(), factory.empty().sumOfSquares(KERNEL));
      assertEquals(0, factory.empty().sumOfSquares(), 0d);
      assertEquals(1d, scalar.sumOfSquares(), 0);
      assertEquals(4, rowVector.sumOfSquares(), 0);
      assertEquals(4, colVector.sumOfSquares(), 0);
      assertEquals(650, matrix34.sumOfSquares(), 0);

      assertEquals(factory.array(new double[]{107, 140, 179, 224}),
                   matrix34.sumOfSquares(ROW));

      assertEquals(factory.array(new double[]{30, 174, 446}),
                   matrix34.sumOfSquares(COLUMN));

      assertEquals(factory.array(new double[]{650}),
                   matrix34.sumOfSquares(ROW, COLUMN));

      assertEquals(factory.array(new double[][]{
            {2164, 2320, 2484, 2656},
            {2836, 3024, 3220, 3424},
            {3636, 3856, 4084, 4320}
      }), tensor2234.sumOfSquares(KERNEL, CHANNEL));
   }

   @Test
   public void toStringTest() {
      assertEquals("array({}, shape=(), dType='" + factory.getType().getSimpleName() + "', weight=1.000000)", factory
            .zeros(0).toString());
   }

   @Test
   public void toTensor() {
      assertEquals(tensor234, nd.convertTensor(tensor234.toTensor()));
      assertEquals(rowVector, nd.convertTensor(rowVector.toTensor()));
      assertEquals(matrix34, nd.convertTensor(matrix34.toTensor()));
      assertEquals(tensor2234, nd.convertTensor(tensor2234.toTensor()));
   }

   @Test
   public void unitize() {
      NumericNDArray n = factory.create(1, 10, NDArrayInitializer.random());
      assertEquals(1, n.unitize().norm2(), 0.001d);
   }

   @Test
   public void weight() {
      NumericNDArray n = factory.zeros(3, 4);
      n.setWeight(10);
      assertEquals(10, n.getWeight(), 0d);
   }

   @Test
   public void zero() {
      NumericNDArray n = factory.create(5, 4, 23, 4, NDArrayInitializer.random());
      n.zero();
      assertEquals(factory.zeros(5, 4, 23, 4), n);
   }

   @Test
   public void zeroLike() {
      NumericNDArray n = factory.create(5, 4, 23, 4, NDArrayInitializer.random());
      NumericNDArray z = n.zeroLike();
      assertEquals(factory.zeros(5, 4, 23, 4), z);
      assertEquals(n.shape(), z.shape());
   }


}
