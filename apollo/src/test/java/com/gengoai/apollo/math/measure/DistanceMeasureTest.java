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

package com.gengoai.apollo.math.measure;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceMeasureTest {
   private final NumericNDArray v1 = nd.DFLOAT32.array(new float[]{1, 0, 1, 0, 1, 1});
   private final NumericNDArray v4 = nd.DFLOAT32.array(new float[]{0, 1, 0, 1, 0, 0});

   private final NumericNDArray v2 = nd.DFLOAT32.array(new float[]{1, 1, 0, 1, 0, 1});
   private final NumericNDArray v3 = nd.DFLOAT32.array(new float[]{1, 1, 0, 1, 0, 1});


   @Test
   public void euclidean() {
      assertEquals(2, Distance.Euclidean.calculate(v1, v2), 0.01d);
      assertEquals(2, Distance.Euclidean.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-2, Distance.Euclidean.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.Euclidean.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.Euclidean.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.Euclidean.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(2.45, Distance.Euclidean.calculate(v1, v4), 0.01d);
      assertEquals(2.45, Distance.Euclidean.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-2.45, Distance.Euclidean.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void hamming() {
      assertEquals(4, Distance.Hamming.calculate(v1, v2), 0.01d);
      assertEquals(4, Distance.Hamming.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-4, Distance.Hamming.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.Hamming.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.Hamming.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.Hamming.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(6, Distance.Hamming.calculate(v1, v4), 0.01d);
      assertEquals(6, Distance.Hamming.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-6, Distance.Hamming.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void angular() {
      assertEquals(0.67, Distance.Angular.calculate(v1, v2), 0.01d);
      assertEquals(0.67, Distance.Angular.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(0.33, Distance.Angular.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.Angular.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.Angular.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(1.0, Distance.Angular.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(1, Distance.Angular.calculate(v1, v4), 0.01d);
      assertEquals(1, Distance.Angular.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(0, Distance.Angular.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void chebyshev() {
      assertEquals(1, Distance.Chebyshev.calculate(v1, v2), 0.01d);
      assertEquals(1, Distance.Chebyshev.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-1, Distance.Chebyshev.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.Chebyshev.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.Chebyshev.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.Chebyshev.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(1, Distance.Chebyshev.calculate(v1, v4), 0.01d);
      assertEquals(1, Distance.Chebyshev.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-1, Distance.Chebyshev.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void earthMovers() {
      assertEquals(2, Distance.EarthMovers.calculate(v1, v2), 0.01d);
      assertEquals(2, Distance.EarthMovers.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-2, Distance.EarthMovers.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.EarthMovers.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.EarthMovers.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.EarthMovers.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(5, Distance.EarthMovers.calculate(v1, v4), 0.01d);
      assertEquals(5, Distance.EarthMovers.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-5, Distance.EarthMovers.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void KLDivergence() {
      assertEquals(0, Distance.KLDivergence.calculate(v1, v2), 0.01d);
      assertEquals(0, Distance.KLDivergence.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.KLDivergence.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.KLDivergence.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.KLDivergence.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.KLDivergence.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(0, Distance.KLDivergence.calculate(v1, v4), 0.01d);
      assertEquals(0, Distance.KLDivergence.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.KLDivergence.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void manhattan() {
      assertEquals(4, Distance.Manhattan.calculate(v1, v2), 0.01d);
      assertEquals(4, Distance.Manhattan.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-4, Distance.Manhattan.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.Manhattan.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.Manhattan.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.Manhattan.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(6, Distance.Manhattan.calculate(v1, v4), 0.01d);
      assertEquals(6, Distance.Manhattan.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-6, Distance.Manhattan.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

   @Test
   public void squaredEuclidean() {
      assertEquals(4, Distance.SquaredEuclidean.calculate(v1, v2), 0.01d);
      assertEquals(4, Distance.SquaredEuclidean.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-4, Distance.SquaredEuclidean.asSimilarityMeasure().calculate(v1, v2), 0.01d);

      assertEquals(0, Distance.SquaredEuclidean.calculate(v3, v2), 0.01d);
      assertEquals(0, Distance.SquaredEuclidean.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(-0, Distance.SquaredEuclidean.asSimilarityMeasure().calculate(v3, v2), 0.01d);

      assertEquals(6, Distance.SquaredEuclidean.calculate(v1, v4), 0.01d);
      assertEquals(6, Distance.SquaredEuclidean.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0.01d);
      assertEquals(-6, Distance.SquaredEuclidean.asSimilarityMeasure().calculate(v1, v4), 0.01d);
   }

}
